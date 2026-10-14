// Global bridge functions callable by Android Native layer

function DisplayMessage(message) {
    var siriText = document.getElementById("SiriMessageText");
    if (siriText) {
        siriText.innerText = message;
    }
    var statusText = document.getElementById("StatusText");
    if (statusText) {
        statusText.innerText = message;
    }
}

function ShowSiri() {
    $("#Oval").attr("hidden", true);
    $("#SiriWave").attr("hidden", false);
}

function ShowHood() {
    $("#Oval").attr("hidden", false);
    $("#SiriWave").attr("hidden", true);
}

function senderText(message) {
    var chatBox = document.getElementById("chat-canvas-body");
    if (chatBox && message && message.trim() !== "") {
        chatBox.innerHTML += `
        <div class="row justify-content-end mb-4">
            <div class="width-size">
                <div class="sender_message">${message}</div>
            </div>
        </div>`;
        chatBox.scrollTop = chatBox.scrollHeight;
    }
}

function receiverText(message) {
    var chatBox = document.getElementById("chat-canvas-body");
    if (chatBox && message && message.trim() !== "") {
        chatBox.innerHTML += `
        <div class="row justify-content-start mb-4">
            <div class="width-size">
                <div class="receiver_message">${message}</div>
            </div>
        </div>`;
        chatBox.scrollTop = chatBox.scrollHeight;
    }
}