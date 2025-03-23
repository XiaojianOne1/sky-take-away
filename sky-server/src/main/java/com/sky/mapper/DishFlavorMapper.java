package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    void insertBatch(@Param("flavorList") List<DishFlavor> flavorList);

    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(@Param("dishId") Long id);
}
