package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ClassName  DishServiceImpl
 * Author  xiaojianOne
 * Date  2025/3/22 22:04
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
public class DishServiceImpl implements DishService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增菜品
     *
     * @param dishDTO
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO, dish);

        //向菜品表插入一条数据
        dishMapper.insert(dish);

        Long dishId = dish.getId();

        List<DishFlavor> flavorList = dishDTO.getFlavors();
        if (flavorList != null && flavorList.size() > 0) {

            for (DishFlavor dishFlavor : flavorList) {
                dishFlavor.setDishId(dishId);
            }

            //向口味表中插入n条数据
            dishFlavorMapper.insertBatch(flavorList);
        }

    }


    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {

        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        long total = page.getTotal();
        List<DishVO> dishList = page.getResult();

        return new PageResult(total, dishList);
    }


    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {

        //判断当前菜品是否能够被删除,是否存在起售中的菜品
        for (Long id : ids) {

            Dish dish = dishMapper.selectById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断当前菜品是否被套餐关联 若关联则不能删除
        for (Long id : ids) {

            Long count = setmealDishMapper.selectCountByDishId(id);
            if (count > 0) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
        }

        for (Long id : ids) {
            //删除菜品表中的菜品数据
            dishMapper.deleteById(id);
            //删除口味表中的菜品数据
            dishFlavorMapper.deleteByDishId(id);
        }
    }


    /**
     * 根据id查询菜品 (回显)
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {

        //直接三张表联合查询 dish 、dish_flavor 和 category 表
        DishVO dishVO = dishMapper.getByIdWithFlavor(id);

        return dishVO;
    }


    /**
     * 根据id更新菜品
     *
     * @param dishDTO
     */
    @Transactional
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.update(dish);

        //删除原有口味数据
        dishFlavorMapper.deleteByDishId(dish.getId());

        //重新插入口味数据
        List<DishFlavor> flavorList = dishDTO.getFlavors();
        if (flavorList != null && flavorList.size() > 0) {

            for (DishFlavor dishFlavor : flavorList) {
                dishFlavor.setDishId(dish.getId());
            }

            //向口味表中插入n条数据
            dishFlavorMapper.insertBatch(flavorList);
        }

    }


    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {

        Dish dish = Dish.builder().categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();

        return dishMapper.selectByCategoryId(dish);
    }


    /**
     * 菜品启售/停售
     *
     * @param id
     * @param status
     */
    @Override
    public void status(Long id, Integer status) {

        Dish dish = Dish.builder().
                id(id).
                status(status).build();

        dishMapper.update(dish);

        //假如菜品停售,则套餐中也要停售
        //查一下套餐中是否有该菜品
        //假如有套餐中有该菜品，则套餐也要停售
        if (status == StatusConstant.DISABLE) {
            //获取了有停售菜品的 套餐菜品关联表 数据
            List<SetmealDish> setmealDishes = setmealDishMapper.selectByDishId(id);
            for (SetmealDish setmealDish : setmealDishes) {
                //获取了有停售菜品的套餐id
                Long setmealId = setmealDish.getSetmealId();

                Setmeal setmeal = Setmeal.builder().
                        id(setmealId).
                        status(StatusConstant.DISABLE).
                        build();
                setmealMapper.update(setmeal);
            }
        }
    }
}
