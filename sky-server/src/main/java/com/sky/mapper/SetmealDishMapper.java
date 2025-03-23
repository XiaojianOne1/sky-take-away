package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealDishMapper {


    @Select("select count(*) from setmeal_dish where dish_id = #{id}")
    Long selectCountByDishId(Long id);
}
