document.addEventListener('DOMContentLoaded', fetchVisitors);

async function fetchVisitors() {
    const visitorsTableBody = document.querySelector('#visitorsTable tbody');
    const messageDiv = document.getElementById('message');
    visitorsTableBody.innerHTML = ''; // 清空现有内容
    messageDiv.textContent = '';

    try {
        const response = await fetch('/api/admin/visitors/pending-approved');
        const visitors = await response.json();

        if (response.ok && visitors.length > 0) {
            visitors.forEach(visitor => {
                const row = visitorsTableBody.insertRow();
                row.insertCell(0).textContent = visitor.id;
                row.insertCell(1).textContent = visitor.name;
                row.insertCell(2).textContent = visitor.phone;
                row.insertCell(3).textContent = visitor.visitedPerson + (visitor.visitedPersonDepartment ? ` (${visitor.visitedPersonDepartment})` : '');
                row.insertCell(4).textContent = visitor.visitDate;
                row.insertCell(5).textContent = visitor.status;

                const actionCell = row.insertCell(6);
                const detailButton = document.createElement('button');
                detailButton.textContent = '查看详情';
                detailButton.onclick = () => {
                    window.location.href = `admin-detail.html?id=${visitor.id}`;
                };
                actionCell.appendChild(detailButton);
            });
        } else if (response.ok && visitors.length === 0) {
            messageDiv.textContent = '目前没有待审核或已通过的访客预约。';
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