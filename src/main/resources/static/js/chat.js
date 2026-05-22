document.addEventListener("DOMContentLoaded", function () {
    initThemeToggle();
    initNewChatForm();
    initDocumentUpload();
    initMessageInput();
});

function isEnterKey(event) {
    return event.key === "Enter"
        || event.code === "Enter"
        || event.keyCode === 13
        || event.which === 13;
}

function bindEnterKey(element, onEnter) {
    let handledOnKeyDown = false;

    function handleKeyDown(event) {
        if (!isEnterKey(event) || event.shiftKey) {
            return;
        }
        handledOnKeyDown = true;
        event.preventDefault();
        event.stopPropagation();
        onEnter();
    }

    function handleKeyUp(event) {
        if (!isEnterKey(event) || event.shiftKey) {
            return;
        }
        if (handledOnKeyDown) {
            handledOnKeyDown = false;
            return;
        }
        event.preventDefault();
        event.stopPropagation();
        onEnter();
    }

    element.addEventListener("keydown", handleKeyDown, true);
    element.addEventListener("keyup", handleKeyUp, true);
}

function submitForm(form) {
    if (typeof form.requestSubmit === "function") {
        form.requestSubmit();
    } else {
        form.submit();
    }
}

function initNewChatForm() {
    const form = document.getElementById("new-chat-form");
    const titleInput = document.getElementById("new-chat-title");
    if (!form || !titleInput) {
        return;
    }

    bindEnterKey(titleInput, function () {
        if (!titleInput.value.trim()) {
            return;
        }
        submitForm(form);
    });
}

function initDocumentUpload() {
    const uploadButton = document.getElementById("document-upload-button");
    const fileInput = document.getElementById("document-upload");
    const statusElement = document.getElementById("document-upload-status");
    if (!uploadButton || !fileInput || !statusElement) {
        return;
    }

    uploadButton.addEventListener("click", async function () {
        const file = fileInput.files[0];
        if (!file) {
            statusElement.textContent = "Выберите файл";
            return;
        }

        statusElement.textContent = "Загрузка...";
        uploadButton.disabled = true;
        const formData = new FormData();
        formData.append("file", file);

        try {
            const response = await fetch("/api/documents", {
                method: "POST",
                body: formData
            });
            if (!response.ok) {
                const contentType = response.headers.get("content-type") || "";
                let message = "Ошибка загрузки";
                if (contentType.includes("application/json")) {
                    const body = await response.json().catch(() => ({}));
                    message = body.detail || body.message || message;
                } else {
                    const text = await response.text().catch(() => "");
                    if (text)
                        message = text;
                }
                statusElement.textContent = message;
                return;
            }
            statusElement.textContent = "Документ загружен";
            fileInput.value = "";
        } catch (error) {
            console.error("Ошибка загрузки документа:", error);
            statusElement.textContent = "Ошибка загрузки";
        } finally {
            uploadButton.disabled = false;
        }
    });
}

function initMessageInput() {
    const sendButton = document.getElementById("send-button");
    const chatInput = document.getElementById("chat-input");
    const messagesContainer = document.getElementById("messages");
    if (!sendButton || !chatInput || !messagesContainer) {
        return;
    }

    function sendMessage() {
        const prompt = chatInput.value.trim();
        if (!prompt) {
            return;
        }
        chatInput.value = "";

        const userDiv = document.createElement("div");
        userDiv.className = "message user";
        userDiv.innerHTML = `<img src="/images/user.png" alt="User"><div class="bubble">${escapeHtml(prompt)}</div>`;
        messagesContainer.appendChild(userDiv);

        const pathParts = window.location.pathname.split("/").filter(Boolean);
        const chatId = pathParts[pathParts.length - 1];
        const url = `/chat/${chatId}/stream?prompt=${encodeURIComponent(prompt)}`;

        const eventSource = new EventSource(url);
        let fullText = "";

        const aiDiv = document.createElement("div");
        aiDiv.className = "message mentor";
        aiDiv.innerHTML = `<img src="/images/mentor.png" alt="Mentor">`;
        const aiBubble = document.createElement("div");
        aiBubble.className = "bubble";
        aiDiv.appendChild(aiBubble);
        messagesContainer.appendChild(aiDiv);

        eventSource.onmessage = function (event) {
            const data = JSON.parse(event.data);
            fullText += data.text;
            aiBubble.innerHTML = marked.parse(fullText);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        };

        eventSource.onerror = function (e) {
            console.error("Ошибка SSE:", e);
            eventSource.close();
        };
    }

    sendButton.addEventListener("click", sendMessage);
    bindEnterKey(chatInput, sendMessage);
}

function escapeHtml(text) {
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}

function initThemeToggle() {
    const toggle = document.getElementById("theme-toggle");
    if (!toggle) {
        return;
    }

    toggle.addEventListener("click", function () {
        const isDark = document.documentElement.getAttribute("data-theme") === "dark";
        if (isDark) {
            document.documentElement.removeAttribute("data-theme");
            localStorage.setItem("theme", "light");
        } else {
            document.documentElement.setAttribute("data-theme", "dark");
            localStorage.setItem("theme", "dark");
        }
    });
}
