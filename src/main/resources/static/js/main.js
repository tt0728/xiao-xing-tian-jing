document.getElementById('getVisitorsBtn').addEventListener('click', fetchVisitors);
const btn = document.getElementById('getVisitorsBtn');
if (btn) {
    btn.addEventListener('click', fetchVisitors);
}

async function fetchVisitors() {
    try {
        const response = await fetch('/admin/visitors/all'); 
        if (!response.ok) { // 检查响应状态码
            const errorText = await response.text();
            throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
        }
        const visitors = await response.json();

        const visitorListDiv = document.getElementById('visitorList');
        visitorListDiv.innerHTML = '';
        visitorListDiv.innerHTML = '<h2>访客列表：</h2>';
        if (visitors && visitors.length > 0) {
            const ul = document.createElement('ul');
            visitors.forEach(visitor => {
                const li = document.createElement('li');
                li.textContent = `ID: ${visitor.id}, 姓名: ${visitor.name}, 状态: ${convertStatus(visitor.status)}, 审批意见: ${visitor.comments || '无'}`;
                ul.appendChild(li);
            });
            visitorListDiv.appendChild(ul);
        } else {
            visitorListDiv.innerHTML += '<p>暂无访客。</p>';
        }

    } catch (error) {
        console.error('获取访客列表失败:', error);
        document.getElementById('visitorList').innerHTML = '<p style="color: red;">获取访客列表失败，请检查后端服务是否运行。</p>';
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