package com.example.demo.controller;

import com.example.demo.model.Department;
import com.example.demo.model.Employee;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class DepartmentController {
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentController(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/departments")
    public String listDepartments(Model model) {
        List<Department> departments = departmentRepository.findAll();
        model.addAttribute("departments", departments);
        return "departmentList";
    }

    @GetMapping("/departments/{id}/employees")
    public String listEmployeesInDepartment(@PathVariable Integer id, Model model) {
        Department department = departmentRepository.findById(id).orElseThrow();
        List<Employee> employees = employeeRepository.findByDepartmentId(id);
        model.addAttribute("department", department);
        model.addAttribute("employees", employees);
        return "employeeList";
    }
}
