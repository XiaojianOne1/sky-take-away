package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ClassName  SetmealServiceImpl
 * Author  xiaojianOne
 * Date  2025/3/24 8:53
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
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;


    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //向套餐表插入数据
        setmealMapper.insert(setmeal);

        //获取生成的套餐id
        Long setmealId = setmeal.getId();

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }

        //保存套餐和菜品的关联关系
        setmealDishMapper.insertBatch(setmealDishes);
    }


    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {

        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page =setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }


    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    public void deleteBatch(List<Long> ids) {

        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (StatusConstant.ENABLE == setmeal.getStatus()){
                //起售中的套餐不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        for (Long setmealId : ids) {

            //删除套餐表中的数据
            setmealMapper.deleteById(setmealId);
            //删除套餐菜品关系表中的数据
            setmealDishMapper.deleteBySetmealId(setmealId);
        }
    }


    /**
     * 根据id查询套餐详情
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {

        //获取套餐详情
        Setmeal setmeal = setmealMapper.getById(id);
        //根据套餐id 从关系表中 查询菜品详情
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }


    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //修改套餐表
        setmealMapper.update(setmeal);

        //套餐id
        Long setmealId = setmealDTO.getId();

        //2、删除套餐和菜品的关联关系，操作setmeal_dish表，执行delete
        setmealDishMapper.deleteBySetmealId(setmealId);

        //设置每个 套餐和菜品的关联表 的 setmealId
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }

        //3、重新插入套餐和菜品的关联关系，操作setmeal_dish表，执行insert
        setmealDishMapper.insertBatch(setmealDishes);

    }


    /**
     * 套餐起售、停售
     * @param status
     * @param id 套餐id
     *    实现逻辑
     *  - 可以对状态为起售的套餐进行停售操作，可以对状态为停售的套餐进行起售操作
     *  - 起售的套餐可以展示在用户端，停售的套餐不能展示在用户端
     *  - 起售套餐时，如果套餐内包含停售的菜品，则不能起售
     */
    @Override
    public void startOrStop(Integer status, Long id) {

        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(status == StatusConstant.ENABLE){

            //根据套餐id查询菜品集合
            List<Dish> dishList = dishMapper.getBySetmealId(id);

            if(dishList != null && dishList.size() > 0) {
                //判断菜品集合中是否有停售菜品
                for (Dish dish : dishList) {
                    if(dish.getStatus() == StatusConstant.DISABLE){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                }
            }
        }

        //套餐内 没有 停售菜品 则可以修改套餐状态
        Setmeal setmeal = Setmeal.builder().id(id).
                status(status).
                build();

        setmealMapper.update(setmeal);
    }


    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }


    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
