package com.zzyl.nursing.service.impl;

import java.util.List;
import com.zzyl.common.utils.DateUtils;
import com.zzyl.common.utils.bean.BeanUtils;
import com.zzyl.nursing.dto.NursingPlanDto;
import com.zzyl.nursing.mapper.NursingProjectPlanMapper;
import com.zzyl.nursing.vo.NursingPlanVo;
import com.zzyl.nursing.vo.NursingProjectPlanVo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.mapper.NursingPlanMapper;
import com.zzyl.nursing.domain.NursingPlan;
import com.zzyl.nursing.service.INursingPlanService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * 护理计划Service业务层处理
 *
 * @author luxingzeng
 * @date 2026-10-02
 */
@Service
public class NursingPlanServiceImpl extends ServiceImpl<NursingPlanMapper, NursingPlan> implements INursingPlanService
{
    @Autowired
    private NursingProjectPlanMapper nursingProjectPlanMapper;

    @Autowired
    private NursingPlanMapper nursingPlanMapper;

    /**
     * 查询护理计划
     *
     * @param id 护理计划主键
     * @return 护理计划
     */
    @Override
    public NursingPlanVo selectNursingPlanById(Long id)
    {
        // 1.根据id查询护理计划
        NursingPlan nursingPlan = nursingPlanMapper.selectNursingPlanById(id);

        // 2.根据护理计划id查询关联的所有护理项目
        List<NursingProjectPlanVo> list = nursingProjectPlanMapper.selectByPlanId(id);

        // 3.封装结果并返回
        NursingPlanVo nursingPlanVo = new NursingPlanVo();
        BeanUtils.copyProperties(nursingPlan, nursingPlanVo);
        nursingPlanVo.setProjectPlans(list);
        return nursingPlanVo;
    }

    /**
     * 查询护理计划列表
     *
     * @param nursingPlan 护理计划
     * @return 护理计划
     */
    @Override
    public List<NursingPlan> selectNursingPlanList(NursingPlan nursingPlan)
    {
        return nursingPlanMapper.selectNursingPlanList(nursingPlan);
    }

    /**
     * 新增护理计划
     *
     * @param dto 护理计划
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertNursingPlan(NursingPlanDto dto)
    {
        // 保存护理计划

        //1.保存护理计划
        NursingPlan nursingPlan = new NursingPlan();
        // 2.属性拷贝
        BeanUtils.copyProperties(dto, nursingPlan);
        nursingPlan.setCreateTime(DateUtils.getNowDate());

        nursingPlanMapper.insert(nursingPlan);

        // 批量保存护理项目计划关系
        int count = nursingProjectPlanMapper.batchInsert(dto.getProjectPlans(), nursingPlan.getId());
        return count == 0 ? 0 : 1;
    }

    /**
     * 修改护理计划
     *
     * @param dto 护理计划
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateNursingPlan(NursingPlanDto dto)
    {
        try {
            // 判断dto中的项目列表为空，如果不为空，则先删除护理计划与护理项目的关系，然后重新批量添加
            if (dto.getProjectPlans() != null && !dto.getProjectPlans().isEmpty()) {
                // 删除护理计划对应的护理项目列表
                nursingProjectPlanMapper.deleteByPlanId(dto.getId());

                // 批量保存护理计划对应的护理项目列表
                nursingProjectPlanMapper.batchInsert(dto.getProjectPlans(), dto.getId());
            }

            // 属性拷贝
            NursingPlan nursingPlan = new NursingPlan();
            BeanUtils.copyProperties(dto, nursingPlan);

            // 不管项目列表是否为空，都要修改护理计划
            return nursingPlanMapper.updateById(nursingPlan);
        } catch (BeansException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量删除护理计划
     *
     * @param id 需要删除的护理计划主键
     * @return 结果
     */
    @Override
    public int deleteNursingPlanByIds(Long id)
    {
        // 删除护理计划与护理项目的关系
        nursingProjectPlanMapper.deleteByPlanId(id);
        // 删除护理计划
        return removeById(id) ? 1 : 0;
    }

    /**
     * 删除护理计划信息
     *
     * @param id 护理计划主键
     * @return 结果
     */
    @Override
    public int deleteNursingPlanById(Long id)
    {
        return removeById(id) ? 1 : 0;
    }
}