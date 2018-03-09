package com.suixingpay.datas.manager.controller;

import com.suixingpay.datas.manager.core.entity.MrJobTasksSchedule;
import com.suixingpay.datas.manager.service.MrJobTasksScheduleService;
import com.suixingpay.datas.manager.web.message.ResponseMessage;
import com.suixingpay.datas.manager.web.page.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.suixingpay.datas.manager.web.message.ResponseMessage.ok;

/**
 * 任务泳道进度表 controller控制器
 * 
 * @author: FairyHood
 * @date: 2018-03-07 17:26:55
 * @version: V1.0-auto
 * @review: FairyHood/2018-03-07 17:26:55
 */
@Api(description = "任务泳道进度表管理")
@RestController
@RequestMapping("/mrjobtasksschedule")
public class MrJobTasksScheduleController {

    @Autowired
    protected MrJobTasksScheduleService mrJobTasksScheduleService;

    @PostMapping
    @ApiOperation(value = "新增", notes = "新增")
    public ResponseMessage add(@RequestBody MrJobTasksSchedule mrJobTasksSchedule) {
        Integer number = mrJobTasksScheduleService.insert(mrJobTasksSchedule);
        return ok(number);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改", notes = "修改")
    public ResponseMessage update(@PathVariable("id") Long id, @RequestBody MrJobTasksSchedule mrJobTasksSchedule) {
        Integer number = mrJobTasksScheduleService.update(id, mrJobTasksSchedule);
        return ok(number);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除", notes = "删除")
    public ResponseMessage delete(@PathVariable("id") Long id) {
        mrJobTasksScheduleService.delete(id);
        return ok();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "查询明细", notes = "查询明细")
    public ResponseMessage info(@PathVariable("id") Long id) {
        MrJobTasksSchedule mrJobTasksSchedule = mrJobTasksScheduleService.selectById(id);
        return ok(mrJobTasksSchedule);
    }

    @ApiOperation(value = "查询列表", notes = "查询列表")
    @GetMapping
    public ResponseMessage list(
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Page<MrJobTasksSchedule> page = mrJobTasksScheduleService.page(new Page<MrJobTasksSchedule>(pageNo, pageSize));
        return ok(page);
    }

}