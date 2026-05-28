package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.entity.ExaminationItem;
import com.example.keshe1.mapper.ExaminationItemMapper;
import com.example.keshe1.service.ExaminationItemService;
import org.springframework.stereotype.Service;

/**
 * 检查项目服务实现类
 */
@Service
public class ExaminationItemServiceImpl extends ServiceImpl<ExaminationItemMapper, ExaminationItem> implements ExaminationItemService {
}