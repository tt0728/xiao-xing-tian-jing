package com.example.demo.service;

import com.example.demo.module.Approval;
import com.example.demo.module.Companion;
import com.example.demo.module.Visitor;
import com.example.demo.repository.VisitorRepository;
import com.example.demo.dto.VisitorRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VisitorService {

    private final VisitorRepository visitorRepository;

    @Autowired
    public VisitorService(VisitorRepository visitorRepository) {
        this.visitorRepository = visitorRepository;
    }

    // 访客预约提交，现在接受 visitorAccountId
    @Transactional
    public Visitor submitAppointment(VisitorRequest request, Long visitorAccountId) {
        Visitor visitor = new Visitor();
        visitor.setVisitorAccountId(visitorAccountId); // 设置关联的访客账号ID
        visitor.setName(request.getName());
        visitor.setPhone(request.getPhone());
        visitor.setIdCard(request.getIdCard());
        visitor.setVisitedPerson(request.getVisitedPerson());
        visitor.setVisitedPersonDepartment(request.getVisitedPersonDepartment());
        visitor.setVisitDate((request.getVisitDate()));
        visitor.setVisitTimeStart((request.getVisitTimeStart()));
        visitor.setVisitTimeEnd((request.getVisitTimeEnd()));
        visitor.setPurpose(request.getPurpose());
        visitor.setStatus(Approval.PENDING); // 默认待审核

        // 处理同行人
        if (request.getCompanions() != null && !request.getCompanions().isEmpty()) {
            List<Companion> companions = request.getCompanions().stream()
                    .map(dto -> {
                        Companion companion = new Companion();
                        companion.setName(dto.getName());
                        companion.setPhone(dto.getPhone());
                        companion.setIdCard(dto.getIdCard());
                        companion.setVisitor(visitor); // 设置关联的访客
                        return companion;
                    })
                    .collect(Collectors.toList());
            visitor.setCompanions(companions);
        }

        return visitorRepository.save(visitor);
    }

    // 获取所有待审核或已审核的访客列表 (管理员使用)
    public List<Visitor> getPendingOrApprovedVisitors() {
        return visitorRepository.findByStatusIn(List.of(Approval.PENDING, Approval.APPROVED));
    }

    // 根据访客账号ID获取所有预约
    public List<Visitor> getVisitorsByAccountId(Long visitorAccountId) {
        return visitorRepository.findByVisitorAccountId(visitorAccountId);
    }

    // 获取访客详情
    public Optional<Visitor> getVisitorDetails(Long id) {
        return visitorRepository.findById(id);
    }

    @Transactional
    public Optional<Visitor> approveOrRejectVisitor(Long id, String newStatus, String comments) {
        return visitorRepository.findById(id)
                .map(visitor -> {
                    if ("APPROVED".equalsIgnoreCase(newStatus)) {
                        visitor.setStatus(Approval.APPROVED);
                    } else if ("REJECTED".equalsIgnoreCase(newStatus)) {
                        visitor.setStatus(Approval.REJECTED);
                    }
                    visitor.setComments(comments);
                    return visitorRepository.save(visitor);
                });
    }

    @Transactional
    public Optional<Visitor> recordEntry(String qrCodeContent) {
        // 假设qrCodeContent是Visitor的ID
        try {
            Long visitorId = Long.parseLong(qrCodeContent);
            return visitorRepository.findById(visitorId)
                    .map(visitor -> {
                        if (visitor.getStatus() == Approval.APPROVED && visitor.getEntryTime() == null) {
                            visitor.setEntryTime(LocalTime.now());
                            return visitorRepository.save(visitor);
                        }
                        return null; // 未批准或已入场
                    });
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<Visitor> recordExit(String qrCodeContent) {
        // 假设qrCodeContent是Visitor的ID
        try {
            Long visitorId = Long.parseLong(qrCodeContent);
            return visitorRepository.findById(visitorId)
                    .map(visitor -> {
                        if (visitor.getStatus() == Approval.APPROVED && visitor.getEntryTime() != null
                                && visitor.getExitTime() == null) {
                            visitor.setExitTime(LocalTime.now());
                            return visitorRepository.save(visitor);
                        }
                        return null; // 未批准、未入场或已出场
                    });
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    // 获取所有状态的访客用于管理员审核
    public List<Visitor> getAllVisitorsForAdminReview() {
        return visitorRepository.findAll();
    }
}