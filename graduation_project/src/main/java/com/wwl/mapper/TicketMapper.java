package com.wwl.mapper;

import com.wwl.model.entity.MovieTicket;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 浣滆?咃細鐜嬫枃鏋?
 * 鏃堕棿锛?026 2026/5/9 涓嬪崍1:42
 * 鎻忚堪锛?
 */
@Mapper
public interface TicketMapper {
    @Insert("insert into movie_ticket(cinema_name,movie_name,start_date,start_time,end_time, seat_info,seat_count,seat_price,total_price,all_text)" +
            "values ( #{cinemaName},#{movieName},#{startDate},#{startTime}, #{endTime}, #{seatInfo}, #{seatCount}, #{seatPrice},#{totalPrice},#{allText})")
    int insertTicketPicture(MovieTicket ticket);
}
