package com.example.demo.controller;

import com.example.demo.module.Visitor;
import com.example.demo.service.VisitorService;
import com.example.demo.dto.VisitorRequest; // 导入VisitorRequest DTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // 【解决：Collectors 无法解析】导入 Collectors

@RestController
@RequestMapping("/api") // 将基础路径修改为 /api
public class VisitorController {

    private final VisitorService visitorService;

    @Autowired
    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    // --- 供来访人员使用的 API ---

    // 访客预约提交
    @PostMapping("/public/visitors/register") // 公开接口
    public ResponseEntity<Visitor> registerVisitor(@RequestBody VisitorRequest request) {
        // 【解决：Invalid Character】 检查这一行或附近是否存在不可见字符
        Visitor registeredVisitor = visitorService.submitAppointment(request);
        return new ResponseEntity<>(registeredVisitor, HttpStatus.CREATED);
    }

    // （可选）来访人员查询自己的预约状态，需要提供查询凭证，比如姓名+手机号或者预约ID
    @GetMapping("/public/visitors/query")
    public ResponseEntity<List<Visitor>> queryMyVisitors(@RequestParam String name, @RequestParam String phone) {
        // 【解决：getAllVisitors() 未定义 & equals 错误】
        // 之前我们移除了 getAllVisitors()，但你可能想根据姓名和电话查找。
        // VisitorService 里没有直接的 findByNameAndPhone 方法，可以先在Service层添加，或者在Controller层过滤。
        // 这里为了快速解决编译问题，我们使用 Service 层的 getPendingOrApprovedVisitors()，
        // 然后在 Controller 层进行过滤（实际应用中，这种过滤应该放在 Service 或 Repository 层更高效）。
        List<Visitor> visitors = visitorService.getPendingOrApprovedVisitors().stream() // 使用现有的方法，或者为查询创建新Service方法
                .filter(v -> v.getName().equals(name) && v.getPhone().equals(phone))
                .collect(Collectors.toList());
        if (visitors.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(visitors, HttpStatus.OK);
    }

    // --- 供管理员使用的 API ---

    // 获取所有待审核或已审核的访客列表（简略信息）
    @GetMapping("/admin/visitors/pending-approved")
    public ResponseEntity<List<Visitor>> getPendingOrApprovedVisitors() {
        List<Visitor> visitors = visitorService.getPendingOrApprovedVisitors();
        // 为了简略显示，这里可以创建一个简略信息的DTO来返回，避免返回所有字段
        return new ResponseEntity<>(visitors, HttpStatus.OK);
    }

    // 获取访客预约详情 (包含同行人)
    @GetMapping("/admin/visitors/{id}/details")
    public ResponseEntity<Visitor> getVisitorDetails(@PathVariable Long id) {
        return visitorService.getVisitorDetails(id)
                .map(visitor -> new ResponseEntity<>(visitor, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 审批访客预约 (通过或不通过)
    // 请求体示例: {"status": "APPROVED", "comments": "无"} 或 {"status": "REJECTED",
    // "comments": "访客信息不全"}
    @PutMapping("/admin/visitors/{id}/review")
    public ResponseEntity<Visitor> reviewVisitor(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newStatus = payload.get("status");
        String comments = payload.get("comments");
        if (newStatus == null || (!"APPROVED".equalsIgnoreCase(newStatus) && !"REJECTED".equalsIgnoreCase(newStatus))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return visitorService.approveOrRejectVisitor(id, newStatus, comments)
                .map(visitor -> new ResponseEntity<>(visitor, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // --- 公共 API (扫码进出) ---
    // 访客入场
    @PostMapping("/public/visitors/entry")
    public ResponseEntity<Visitor> recordEntry(@RequestBody Map<String, String> payload) {
        String qrCodeContent = payload.get("qrCodeContent");
        if (qrCodeContent == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return visitorService.recordEntry(qrCodeContent)
                .map(visitor -> new ResponseEntity<>(visitor, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    // 访客离场
    @PostMapping("/public/visitors/exit")
    public ResponseEntity<Visitor> recordExit(@RequestBody Map<String, String> payload) {
        String qrCodeContent = payload.get("qrCodeContent");
        if (qrCodeContent == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return visitorService.recordExit(qrCodeContent)
                .map(visitor -> new ResponseEntity<>(visitor, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }
}