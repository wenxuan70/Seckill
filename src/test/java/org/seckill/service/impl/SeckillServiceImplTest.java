package org.seckill.service.impl;

import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatSeckillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"
})
public class SeckillServiceImplTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        List<Seckill> seckillList = seckillService.getSeckillList();
        logger.info("list={}", seckillList);
    }

    @Test
    public void getById() {
        Seckill seckill = seckillService.getById(1000);
        logger.info("seckill={}", seckill);
    }

    @Test
    public void exportSeckillUrl() {
        int id = 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Exposer exposer = seckillService.exportSeckillUrl(id);
        logger.info("exposer={}", exposer);
        logger.info("start={}", sdf.format(new Date(exposer.getStart())));
        logger.info("end={}", sdf.format(new Date(exposer.getEnd())));
    }

    @Test
    @Disabled
    public void executeSeckill() {
        String md5 = "7a6d03491eb5ba7283d07fdde6e24e0f";
        int id = 1000;
        long phone = 188123456789L;
        SeckillExecution execution =
                seckillService.executeSeckill(id, phone, md5);
        logger.info("result={}", execution);
    }

    @Test
    public void testSeckillLogic() {
        int id = 1003;
        long phone = 13512345678L;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if (exposer.isExposed()) {
            logger.info("exposer={}", exposer);
            // 秒杀开始
            String md5 = exposer.getMd5();
            try {
                SeckillExecution execution = seckillService.executeSeckill(id, phone, md5);
                logger.info("execution={}", execution);
            } catch (SeckillCloseException e) {
                logger.error(e.getMessage());
            } catch (RepeatSeckillException e) {
                logger.error(e.getMessage());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.warn("exposer={}", exposer);
        }
    }

    @Test
    public void executeSeckillProcedure() {
        int id = 1002;
        long phone = 18612345678L;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if (exposer.isExposed()) {
            SeckillExecution execution =
                    seckillService.executeSeckillProcedure(id, phone, exposer.getMd5());
            logger.info("execution={}", execution);
        } else {
            logger.info("exposer={}", exposer);
        }
    }

}