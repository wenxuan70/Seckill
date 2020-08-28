//存放主要交互逻辑js代码
// js模块化
var seckill = {
    //封装秒杀相关ajax的url
    URL: {
        now: function () {
            return '/seckill/time/now';
        },
        exposer: function (seckillId) {
            return '/seckill/' + seckillId + '/exposer'
        },
        killUrl: function (seckillId, md5) {
            return '/seckill/' + seckillId + '/' + md5 + '/execution';
        }
    },
    //验证手机号
    vaildatePhone: function (phone) {
        if (phone && phone.length == 11 && !isNaN(phone)) {
            return true;
        } else {
            return false;
        }
    },
    //开始秒杀
    handleSeckill: function (seckillId, node) {
        node.hide()
            .html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');
        //获取秒杀地址
        $.post(seckill.URL.exposer(seckillId), {}, function (result) {
            if (result && result['success'] === true) {
                var exposer = result['data'];
                if (exposer['exposed']) {
                    //秒杀开始
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.killUrl(seckillId, md5);
                    console.log("killUrl:" + killUrl);
                    //只允许点击一次
                    $('#killBtn').one('click', function () {
                        //禁用秒杀按钮
                        $(this).addClass('disabled');
                        //发送秒杀请求
                        $.post(killUrl, {}, function (res) {
                            if (res && res['success'] === true) {
                                var execution = res['data'];
                                var state = execution['state'];
                                var stateInfo = execution['stateInfo'];
                                // 展示秒杀结果
                                node.html('<label class="label label-success">' + stateInfo + '</label>');
                            } else {
                                alert(res['error']);
                            }
                        });
                    });
                    //显示
                    node.show();
                } else {
                    //重新计时
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    seckill.countdown(seckillId, now, start, end);
                }
            } else {
                console.log('result:' + result);
            }
        });
    },
    //秒杀倒计时
    countdown: function (seckillId, nowTime, startTime, endTime) {
        //时间判断
        var seckillBox = $('#seckill-box');
        if (nowTime > endTime) {
            // 秒杀结束
            seckillBox.html('秒杀结束!');
        } else if (nowTime < startTime) {
            //秒杀未开始，倒计时
            var killTime = new Date(startTime + 1000);
            seckillBox.countdown(killTime, function (event) {
                //时间格式化
                var format = event.strftime('秒杀倒计时: %D天 %H时 %M分 %S秒');
                seckillBox.html(format);
            }).on('finish.countdown', function () {
                //到达指定时间后，回调事件
                //获取秒杀地址
                seckill.handleSeckill(seckillId, seckillBox);
            });
        } else {
            // 秒杀开始
            seckill.handleSeckill(seckillId, seckillBox);
        }
    },
    //详情页秒杀逻辑
    detail: {
        //详情页初始化
        init: function (params) {
            //用户手机验证和登录，计时交互
            var killPhone = $.cookie('killPhone');
            //验证手机号
            if (!seckill.vaildatePhone(killPhone)) {
                //绑定手机号
                //控制输出
                var killPhoneModal = $('#killPhoneModal');
                killPhoneModal.modal({
                    show: true, // 显示弹出层
                    backdrop: 'static', //禁止位置关闭
                    keyvboard: false //关闭键盘事件
                });
                $('#killPhoneBtn').click(function () {
                    var inputPhone = $('#killPhoneKey').val();
                    if (seckill.vaildatePhone(inputPhone)) {
                        //手机号写入cookie
                        $.cookie('killPhone', inputPhone, {expirse: 7, path: '/seckill'});
                        //刷新页面
                        window.location.reload();
                    } else {
                        //显示错误消息
                        $('#killPhoneMessage')
                            .hide()
                            .html('<label class="label label-danger">手机号错误</label>')
                            .show(300);
                    }
                });
            }
            //已经登录，计时交互
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
            $.get(seckill.URL.now(), {}, function (result) {
                if (result && result['success']) {
                    var nowTime = result['data'];
                    seckill.countdown(seckillId, nowTime, startTime, endTime);
                } else {
                    console.log('result:' + result);
                }
            });
        }
    }
}