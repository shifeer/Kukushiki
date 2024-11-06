document.getElementById('uploadFile').addEventListener('submit', async function(event) {
    event.preventDefault(); // Предотвращаем стандартную отправку формы

    const form = document.getElementById('uploadFile');
    const formData = new FormData(form);

    try {
        // Отправляем форму через AJAX (fetch)
        const uploadResponse = await fetch('/api/v1/transcription/new', {
            method: 'POST',
            body: formData
        });

        if (uploadResponse.ok) {
            const result = await uploadResponse.json();
            const taskId = result.taskId;
            document.getElementById('statusMessage').textContent = 'Файл загружен. Проверяем статус...';

            // Начинаем проверку статуса файла
            checkFileStatus(taskId);
        } else {
            document.getElementById('statusMessage').textContent = 'Ошибка загрузки файла.';
        }
    } catch (error) {
        console.error('Ошибка загрузки файла:', error);
        document.getElementById('statusMessage').textContent = 'Ошибка при отправке запроса.';
    }
});

// Функция для проверки статуса файла
function checkFileStatus(taskId) {
    const intervalId = setInterval(async () => {
        try {
            const response = await fetch(`/api/v1/transcription/getText${taskId}`, {
                method: 'GET'
            });

            if (response.ok) {
                const result = await response.json(); // Получаем JSON-ответ

                if (result.description === "Task is processing") {
                    console.log("Задача в процессе...");
                    document.getElementById('statusMessage').textContent = "Задача в процессе...";
                } else if (result.description === "Task is done") {
                    console.log("Задача завершена!");
                    document.getElementById('statusMessage').textContent = "Задача завершена!";
                    document.getElementById('res').textContent = result.taskIdResult;
                    clearInterval(intervalId); // Останавливаем проверку, если задача завершена
                }
            } else if (response.status === 400) {
                const result = await response.json();
                console.log("Error");
                document.getElementById('statusMessage').textContent = result.description;
                clearInterval(intervalId);
            } else if (response.status === 500) {
                const result = await response.json();
                console.log("Error server")
                document.getElementById('statusMessage').textContent = result.description;
            } else {
                console.log(response.status);
            }
        } catch (error) {
            console.error('Ошибка проверки статуса:', error);
        }
    }, 10000); // Проверка каждые 10 секунд
}