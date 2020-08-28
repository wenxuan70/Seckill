package org.seckill.service.impl;

import org.apache.commons.collections4.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatSeckillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeckillServiceImpl implements SeckillService {

    private static final Logger logger = LoggerFactory.getLogger(SeckillService.class);

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    //md5盐值字符串,用于混淆md5
    private final String salt = "Asdf41afasd6!#!@32313sa1dqdszfva'ldvsaac3c1/421x112";

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 10);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        //优化点:缓存优化
        //访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            //访问mysql
            seckill = seckillDao.queryById(seckillId);
            //不存在秒杀活动
            if (seckill == null) {
                return new Exposer(false, seckillId);
            }
            //redis缓存
            redisDao.putSeckill(seckill);
        }
        Date start = seckill.getStartTime();
        Date end = seckill.getEndTime();
        Date now = new Date();
        //秒杀未开始/已结束
        if (now.getTime() < start.getTime()
                || now.getTime() > end.getTime()) {
            return new Exposer(false, seckillId, now.getTime(),
                    start.getTime(), end.getTime());
        }
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    /**
     * 获取秒杀地址
     *
     * @param seckillId
     * @return
     */
    private String getMD5(long seckillId) {
        String base = seckillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    /**
     * 使用注解声明式事务的好处
     * 1. 团队达成一致约定，统一代码风格。
     * 2. 保证事务方法的执行时间够短。
     * 3. 不是所有的方法都需要事务，如：只有一条修改，只读操作不需要事务控制。
     */
    @Override
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatSeckillException, SeckillCloseException {
        //优化点：
        // 1.先插入购买明细，在减少库存。
        try {
            if (md5 == null || !md5.equals(getMD5(seckillId))) {
                throw new SeckillException("seckill data rewrite");
            }
            //执行秒杀逻辑,减库存 + 记录购买行为
            Date now = new Date();
            //记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            //唯一:seckillId + userPhone
            if (insertCount <= 0) {
                //重复秒杀
                throw new RepeatSeckillException("seckill repeate");
            } else {
                //减库存
                int updateCount = seckillDao.reduceNumber(seckillId, now);
                if (updateCount <= 0) {
                    //没有更新到记录,秒杀结束
                    throw new SeckillCloseException("seckill closed");
                } else {
                    // 秒杀成功
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e) {
            throw e;
        } catch (RepeatSeckillException e) {
            throw e;
        } catch (Exception e) {
            //编译期异常转化为运行期异常
            //发生运行期异常,Spring执行事务回滚
            logger.error(e.getMessage(), e);
            throw new SeckillException("seckill inner error: " + e.getMessage());
        }
    }

    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStateEnum.REPEAT_KILL);
        }
        Date killTime = new Date();
        Map<String, Object> map = new HashMap<>(4);
        map.put("seckillId", seckillId);
        map.put("killTime", killTime);
        map.put("userPhone", userPhone);
        map.put("result", null);
        //执行存储过程
        try {
            seckillDao.killByProcedure(map);
            Integer result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled successKilled =
                        successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
            } else {
                return new SeckillExecution(seckillId, SeckillStateEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
        }
    }
}
