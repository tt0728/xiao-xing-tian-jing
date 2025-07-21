document.getElementById('getVisitorsBtn').addEventListener('click', fetchVisitors);

async function fetchVisitors() {
    try {
        // 注意：这里的URL是相对于Spring Boot应用的根路径
        // 如果你的后端API是 /api/visitors，那么前端直接访问 /api/visitors 即可
        const response = await fetch('/api/visitors'); 
        const visitors = await response.json();

        const visitorListDiv = document.getElementById('visitorList');
        visitorListDiv.innerHTML = '<h2>访客列表：</h2>';
        if (visitors && visitors.length > 0) {
            const ul = document.createElement('ul');
            visitors.forEach(visitor => {
                const li = document.createElement('li');
                li.textContent = `ID: ${visitor.id}, 姓名: ${visitor.name}, 状态: ${visitor.status}`;
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