document.addEventListener('DOMContentLoaded', fetchVisitors);

async function fetchVisitors() {
    const visitorsTableBody = document.querySelector('#visitorsTable tbody');
    const messageDiv = document.getElementById('message');
    visitorsTableBody.innerHTML = ''; // 清空现有内容
    messageDiv.textContent = '';

    try {
        const response = await fetch('/admin/visitors/all');
        const visitors = await response.json();

        if (response.ok && visitors.length > 0) {
            visitors.forEach(visitor => {
                const row = visitorsTableBody.insertRow();
                row.insertCell().textContent = visitor.id;
                row.insertCell().textContent = visitor.name;
                row.insertCell().textContent = visitor.phone;
                row.insertCell().textContent = visitor.visitedPerson + (visitor.visitedPersonDepartment ? ` (${visitor.visitedPersonDepartment})` : '');
                row.insertCell().textContent = visitor.visitDate;
                row.insertCell().textContent = convertStatus(visitor.status);
                row.insertCell().textContent = visitor.comments || '';

                const actionCell = row.insertCell();
                const detailButton = document.createElement('button');
                detailButton.textContent = '查看详情';
                detailButton.onclick = () => {
                    window.location.href = `admin-detail.html?id=${visitor.id}`;
                };
                actionCell.appendChild(detailButton);
            });
        } else if (response.ok && visitors.length === 0) {
            messageDiv.textContent = '目前没有访客预约。';
        } else {
            messageDiv.textContent = '加载访客列表失败: ' + (visitors.message || '未知错误');
            messageDiv.classList.add('error');
            console.error('加载访客列表失败:', visitors);
        }
    } catch (error) {
        messageDiv.textContent = '网络错误或服务器无响应。';
        messageDiv.classList.add('error');
        console.error('获取访客列表时发生错误:', error);
    }
}

function convertStatus(status) {
    switch(status) {
        case 'PENDING':
            return '待审核';
        case 'APPROVED':
            return '已通过';
        case 'REJECTED':
            return '已拒绝';
        default:
            return status;
    }
}