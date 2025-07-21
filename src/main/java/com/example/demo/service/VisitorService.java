package com.example.demo.service;

import com.example.demo.module.Companion;
import com.example.demo.module.Visitor;
import com.example.demo.repository.VisitorRepository;
import com.example.demo.dto.VisitorRequest; // 导入新的DTO
import com.example.demo.dto.CompanionDTO; // 导入同行人DTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors; // 导入Collectors

@Service
public class VisitorService {

    private final VisitorRepository visitorRepository;

    @Autowired
    public VisitorService(VisitorRepository visitorRepository) {
        this.visitorRepository = visitorRepository;
    }

    // 1. 来访人员：提交预约 (使用 VisitorRequest DTO)
    @Transactional
    public Visitor submitAppointment(VisitorRequest request) {
        Visitor visitor = new Visitor();
        // 映射 VisitorRequest 到 Visitor 实体
        visitor.setName(request.getName());
        visitor.setPhone(request.getPhone());
        visitor.setIdCard(request.getIdCard());
        visitor.setWorkUnit(request.getWorkUnit());
        visitor.setVisitDate(request.getVisitDate());
        visitor.setVisitTimeStart(request.getVisitTimeStart());
        visitor.setVisitTimeEnd(request.getVisitTimeEnd());
        visitor.setPurpose(request.getPurpose());
        visitor.setVisitedPerson(request.getVisitedPerson());
        visitor.setVisitedPersonDepartment(request.getVisitedPersonDepartment());
        visitor.setStatus("PENDING"); // 默认状态为待审核

        // 生成二维码内容
        visitor.setQrCodeContent(UUID.randomUUID().toString());

        // 处理同行人
        if (request.getCompanions() != null && !request.getCompanions().isEmpty()) {
            List<Companion> companions = request.getCompanions().stream()
                    .map(dto -> {
                        Companion companion = new Companion();
                        companion.setName(dto.getName());
                        companion.setPhone(dto.getPhone());
                        companion.setIdCard(dto.getIdCard());
                        companion.setVisitor(visitor); // 设置关联
                        return companion;
                    })
                    .collect(Collectors.toList());
            visitor.setCompanions(companions);
        }

        return visitorRepository.save(visitor);
    }

    // 2. 管理员：获取所有待审核或已审核的预约简略信息 (用于列表)
    // 可以根据需要过滤状态，或者直接获取所有
    public List<Visitor> getPendingOrApprovedVisitors() {
        // 实际应用中可能需要分页和更复杂的查询条件
        // 比如只返回PENDING和APPROVED状态的
        return visitorRepository.findAll().stream()
                .filter(v -> "PENDING".equalsIgnoreCase(v.getStatus()) || "APPROVED".equalsIgnoreCase(v.getStatus()))
                .collect(Collectors.toList());
    }

    // 3. 管理员：获取访客预约详情 (包含同行人)
    public Optional<Visitor> getVisitorDetails(Long id) {
        // 使用 findById 即可，由于 Visitor 实体中定义了 @OneToMany，JPA 会自动加载 companions
        return visitorRepository.findById(id);
    }

    // 4. 管理员：审批访客预约
    @Transactional
    public Optional<Visitor> approveOrRejectVisitor(Long visitorId, String newStatus, String comments) {
        return visitorRepository.findById(visitorId).map(visitor -> {
            if ("APPROVED".equalsIgnoreCase(newStatus) || "REJECTED".equalsIgnoreCase(newStatus)) {
                visitor.setStatus(newStatus.toUpperCase());
                // 这里可以添加逻辑来记录审批意见（如果Visitor实体中新增字段）
                // visitor.setApprovalComments(comments);
                return visitorRepository.save(visitor);
            }
            return null; // 或者抛出自定义异常
        });
    }

    // 5. 访客入场 (二维码验证) - 保持不变
    @Transactional
    public Optional<Visitor> recordEntry(String qrCodeContent) {
        return visitorRepository.findByQrCodeContent(qrCodeContent).map(visitor -> {
            if ("APPROVED".equalsIgnoreCase(visitor.getStatus())) {
                visitor.setStatus("IN_VISIT");
                visitor.setEntryTime(LocalDateTime.now());
                return visitorRepository.save(visitor);
            }
            return null;
        });
    }

    // 6. 访客离场 - 保持不变
    @Transactional
    public Optional<Visitor> recordExit(String qrCodeContent) {
        return visitorRepository.findByQrCodeContent(qrCodeContent).map(visitor -> {
            if ("IN_VISIT".equalsIgnoreCase(visitor.getStatus())) {
                visitor.setStatus("DEPARTED");
                visitor.setExitTime(LocalDateTime.now());
                return visitorRepository.save(visitor);
            }
            return null;
        });
    }
}