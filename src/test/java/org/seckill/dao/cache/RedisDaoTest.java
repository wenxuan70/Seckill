package org.seckill.dao.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dao.SeckillDao;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-dao.xml"
})
public class RedisDaoTest {

    private static final Logger logger = LoggerFactory.getLogger(RedisDaoTest.class);

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SeckillDao seckillDao;

    @Test
    public void testCache() {
        int id = 1002;
        Seckill seckill = redisDao.getSeckill(id);
        logger.info("redis={}", seckill);
        // 访问redis
        if (seckill == null) {
            seckill = seckillDao.queryById(id);
            logger.info("mysql={}", seckill);
            String result = redisDao.putSeckill(seckill);
            logger.info("result={}", result);
        }
    }
}