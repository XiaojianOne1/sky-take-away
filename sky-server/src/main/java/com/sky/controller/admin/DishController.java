package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * ClassName  DishController
 * Author  xiaojianOne
 * Date  2025/3/22 21:58
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
@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Api(tags = "菜品管理")
public class DishController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){

        dishService.saveWithFlavor(dishDTO);

        // 清除缓存
        String key = "dish_" + dishDTO.getCategoryId();
        clearCache(key);

        return Result.success();
    }


    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询：{}",dishPageQueryDTO);

        PageResult result = dishService.pageQuery(dishPageQueryDTO);

        return Result.success(result);
    }


    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam("ids") List<Long> ids){
        log.info("批量删除菜品：{}",ids);

        dishService.deleteBatch(ids);

        // 清除缓存  清除所有缓存数据
        clearCache("dish_*");

        return Result.success();
    }


    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable("id") Long id) {
        log.info("根据id查询菜品：{}", id);

        DishVO dishVO = dishService.getByIdWithFlavor(id);

        return Result.success(dishVO);
    }


    /**
     * 根据id更新菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("根据id更新菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("根据id更新菜品：{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);

        // 清除缓存  清除所有缓存数据
        clearCache("dish_*");

        return Result.success();
    }


    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(@RequestParam Long categoryId){
        log.info("根据分类id查询菜品：{}",categoryId);

        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }


    /**
     * 菜品启售/停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品启售/停售")
    public Result status(@PathVariable("status") Integer status,@RequestParam("id") Long id){

        log.info("菜品启售/停售：{}{}",id,status);

        dishService.status(id,status);

        clearCache("dish_*");

        return Result.success();
    }


    /**
     * 清理所有缓存数据
     */
    private void clearCache(String pattern) {

        // 清除缓存  清除所有缓存数据
        Set keys = redisTemplate.keys(pattern);

        redisTemplate.delete(keys);
    }
}
