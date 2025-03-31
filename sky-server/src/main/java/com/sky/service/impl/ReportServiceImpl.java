package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ClassName  ReportServiceImpl
 * Author  xiaojianOne
 * Date  2025/3/31 14:20
 * Version  17.0
 */
/*
 * 　　┏┓　　　┏┓+ +
 * 　┏┛┻━━━┛┻┓ + +
 * 　┃　　　　　　　┃
 * 　┃　　　━　　　┃ ++ + + +
 *  ████━████ ┃+
 * 　┃　　　　　　　┃ +
 * 　┃　　　┻　　　┃
 * 　┃　　　　　　　┃ + +
 * 　┗━┓　　　┏━┛
 * 　　　┃　　　┃
 * 　　　┃　　　┃ + + + +
 * 　　　┃　　　┃
 * 　　　┃　　　┃ +                神兽保佑
 * 　　　┃　　　┃                  代码无bug
 * 　　　┃　　　┃　　+
 * 　　　┃　 　　┗━━━┓ + +
 * 　　　┃ 　　　　　　　┣┓
 * 　　　┃ 　　　　　　　┏┛
 * 　　　┗┓┓┏━┳┓┏┛ + + + +
 * 　　　　┃┫┫　┃┫┫
 * 　　　　┗┻┛　┗┻┛+ + + +
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 指定时间内的营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> list = new ArrayList<>();
        list.add(begin);

        //先判断 begin 的时间 早于 end 的时间，以免出现死循环
        if(begin.isBefore(end)){

            while (!begin.equals(end)){

                begin =begin.plusDays(1);
                list.add(begin);
            }
        }


        ArrayList<Double> turnoverList = new ArrayList<>();

        for (LocalDate localDate : list) {

            // localDate 仅有年月日，不带时分秒
            // beginTime 带有 时分秒 的日期格式
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            //查询营业额
            Double turnover = orderMapper.selectTurnover(beginTime, endTime,Orders.COMPLETED);

            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        String dateList = StringUtils.join(list, ",");
        String turnoverSumList = StringUtils.join(turnoverList, ",");

        return TurnoverReportVO.builder()
                .dateList(dateList)
                .turnoverList(turnoverSumList)
                .build();
    }


    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dataList = new ArrayList<>();
        dataList.add(begin);

        //先判断 begin 的时间 早于 end 的时间，以免出现死循环
        if(begin.isBefore(end)){

            while (!begin.equals(end)){

                begin =begin.plusDays(1);
                dataList.add(begin);
            }
        }

        //用来统计每日新增用户数量
        List<Integer> newUserList = new ArrayList<>();
        //用来统计每天总用户数量
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate localDate : dataList) {

            // localDate 仅有年月日，不带时分秒
            // beginTime 带有 时分秒 的日期格式
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            HashMap map = new HashMap();

            //查询总用户数量
            map.put("end",endTime);
            Integer totalUserInteger = userMapper.selectTotalUserAndNewUser(map);
            totalUserList.add(totalUserInteger);

            //新增用户数量
            map.put("begin",endTime);
            Integer newUserInteger = userMapper.selectTotalUserAndNewUser(map);
            newUserList.add(newUserInteger);

        }

        String newDateList = StringUtils.join(dataList, ",");
        String totalUserListStr = StringUtils.join(totalUserList, ",");
        String newUserListStr = StringUtils.join(newUserList, ",");

        //封装结果数据
        return UserReportVO.builder()
                .dateList(newDateList)
                .totalUserList(totalUserListStr)
                .newUserList(newUserListStr)
                .build();
    }

}
