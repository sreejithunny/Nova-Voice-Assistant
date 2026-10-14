$(document).ready(function () {

    // Siri Waveform
    var siriContainer = document.getElementById("siri-container");
    var siriWave = null;
    if (siriContainer && typeof SiriWave !== "undefined") {
        var containerWidth = window.innerWidth || 360;
        siriWave = new SiriWave({
            container: siriContainer,
            width: Math.min(containerWidth - 32, 400),
            height: 120,
            style: "ios9",
            amplitude: 1.5,
            speed: 0.25,
            autostart: true
        });
    }

    // Mic button: Starts voice listening
    $("#MicBtn").click(function () {
        ShowSiri();
        DisplayMessage("Listening...");
        if (window.NovaBridge && window.NovaBridge.startVoiceInput) {
            window.NovaBridge.startVoiceInput();
        } else {
            console.log("NovaBridge not connected (testing in browser)");
        }
    });

    // Chat / text submission
    function sendCommand(message) {
        if (!message || message.trim() === "") return;
        senderText(message);
        $("#chatbox").val("");
        ShowHideButton("");
        ShowSiri();
        DisplayMessage("Processing...");

        if (window.NovaBridge && window.NovaBridge.processCommand) {
            window.NovaBridge.processCommand(message);
        } else {
            setTimeout(function () {
                receiverText("Simulated reply for: " + message);
                ShowHood();
            }, 800);
        }
    }

    function ShowHideButton(text) {
        if (!text || text.length === 0) {
            $("#MicBtn").attr("hidden", false);
            $("#SendBtn").attr("hidden", true);
        } else {
            $("#MicBtn").attr("hidden", true);
            $("#SendBtn").attr("hidden", false);
        }
    }

    $("#chatbox").on("input keyup", function () {
        ShowHideButton($(this).val());
    });

    $("#SendBtn").click(function () {
        sendCommand($("#chatbox").val());
    });

    $("#chatbox").keypress(function (e) {
        if (e.which === 13) {
            sendCommand($("#chatbox").val());
        }
    });

    // Wake word listener toggle
    var wakeActive = true;
    $("#WakeWordToggle").click(function () {
        wakeActive = !wakeActive;
        if (window.NovaBridge && window.NovaBridge.toggleWakeWord) {
            window.NovaBridge.toggleWakeWord(wakeActive);
        }
        if (wakeActive) {
            $(this).html('<i class="bi bi-broadcast"></i> Wake: "Nova"').css("opacity", "1");
        } else {
            $(this).html('<i class="bi bi-slash-circle"></i> Wake: Off').css("opacity", "0.6");
        }
    });

});
