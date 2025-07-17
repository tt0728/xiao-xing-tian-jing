document.addEventListener('DOMContentLoaded', function() {
    // 模拟从后端获取部门数据
    const departments = [
        { id: 1, name: '部门一' },
        { id: 2, name: '部门二' },
        { id: 3, name: '部门三' }
    ];

    const departmentTable = document.getElementById('departmentTable').getElementsByTagName('tbody')[0];
    departments.forEach(department => {
        const row = departmentTable.insertRow();
        const nameCell = row.insertCell(0);
        const actionCell = row.insertCell(1);

        nameCell.textContent = department.name;
        const viewBtn = document.createElement('button');
        viewBtn.textContent = '查看员工';
        viewBtn.addEventListener('click', () => {
            showEmployees(department.id);
        });
        actionCell.appendChild(viewBtn);
    });

    // 模拟从后端获取员工数据
    const employees = {
        1: [
            { name: '张三', gender: '男', phone: '13800138000' },
            { name: '李四', gender: '女', phone: '13900139000' }
        ],
        2: [
            { name: '王五', gender: '男', phone: '13700137000' },
            { name: '赵六', gender: '女', phone: '13600136000' }
        ],
        3: [
            { name: '钱七', gender: '男', phone: '13500135000' },
            { name: '孙八', gender: '女', phone: '13400134000' }
        ]
    };

    function showEmployees(departmentId) {
        const employeeTable = document.getElementById('employeeTable').getElementsByTagName('tbody')[0];
        employeeTable.innerHTML = '';
        employees[departmentId].forEach(employee => {
            const row = employeeTable.insertRow();
            const nameCell = row.insertCell(0);
            const genderCell = row.insertCell(1);
            const phoneCell = row.insertCell(2);

            nameCell.textContent = employee.name;
            genderCell.textContent = employee.gender;
            phoneCell.textContent = employee.phone;
        });

        const modal = document.getElementById('employeeModal');
        modal.style.display = 'block';

        const closeBtn = document.getElementsByClassName('close')[0];
        closeBtn.onclick = function() {
            modal.style.display = 'none';
        }

        window.onclick = function(event) {
            if (event.target == modal) {
                modal.style.display = 'none';
            }
        }
    }
});