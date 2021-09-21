let refUsers;
let refChat;
let chatroom;
let sendTo;
let raiseHand = false;
let currentDocID;
let teacher = false;
let firstReport = true;

const TOAST_ENTER_ROOM = 1;
const TOAST_LEAVE_ROOM = 2;
const TOAST_RAISE_HAND = 3;
const TOAST_CHAT_ROOM = 4;
const HAND_UP = 1;
const HAND_DOWN = 0;

function showToast(data) {
    if (data.type === TOAST_ENTER_ROOM) {
        $("#toast-body").text("Vào phòng")
        $("#toast-title").text(data.uname)
    } else if (data.type === TOAST_LEAVE_ROOM) {
        $("#toast-body").text("Rời phòng")
        $("#toast-title").text(data.uname)
    } else if (data.type === TOAST_RAISE_HAND) {
        $("#toast-body").text("Giơ tay")
        $("#toast-title").text(data.uname)
    } else if (data.type === TOAST_CHAT_ROOM) {
        $("#toast-body").text(data.message)
        $("#toast-title").text(data.uname)
    }
    new bootstrap.Toast(document.getElementById('liveToast')).show()

}

function getSubmitedObjects(step, arr) {
    for (const e of arr) {
        if (e.step === "" + step)
            return e;
    }
    return null;
}

function enterLab(user) {
    $('#main').show();
    $('#drawer').show();

    firebase.firestore().collection("labs").doc(getDocID()).get().then((doc) => {  //Đọc thông tin để bắt đầu vào phòng Lab chung
        if (doc.exists) {
            let obj = doc.data();
            currentDocID = obj.docID;
            if (obj.userID === user.uid || hashCode(user.email) == "-448897477") { //Teacher
                $("#btnUpdate").removeClass("d-none")
            }
        }
    })

}

function enterRoom(user) {

    let db = firebase.firestore();
    db.collection("rooms").doc(getRoomID()).get().then((doc) => {  //Đọc thông tin để bắt đầu vào phòng học
        if (doc.exists) {
            let obj = doc.data();
            currentDocID = obj.docID;
            $('#main').show();
            $('#drawer').show();
            $(".room").removeClass("d-none")
            if (obj.userID === user.uid || hashCode(user.email) == "-448897477") { //Teacher
                $(".teacher").removeClass("d-none")
                teacher = true;
                $(".survey-question-wrapper h4").append(" <a href='#' class='show-result' onclick='showQuizResult(this)'>Kết quả</a>")
            }
            $("#btnSubmit").removeClass("d-none")
            $("#btnRaiseHand").removeClass("d-none")
            realtime(user); //Gia nhập phòng realtime
        }
    })

    //Ghi log vao firestore
    db.collection("rooms").doc(getRoomID()).collection("logs").doc(currentUser.uid).set({
        lastEnter: firebase.firestore.FieldValue.serverTimestamp(),
        userName: user.displayName,
        email: user.email
    }, {merge: true});

    //Load cac bài tập đã nộp
    $("google-codelab-survey").each(function () {

        let text_area = $(this).find("textarea");
        let survey_id = $(this).attr("survey-id")
        let ref = firebase.firestore().collection("rooms").doc(getRoomID()).collection("surveys").doc(survey_id).collection("answers").doc(currentUser.uid)

        ref.get().then((doc) => {
            if (doc.exists) {
                let obj = doc.data();
                if (obj.choice != null) { //Cau hoi trac nghiem
                    $(this).find(".form-check-input").eq(obj.choice).prop("checked", true);


                } else if (obj.content != null) {  //Cau hoi dai
                    text_area.val(decodeHtml(obj.content))
                } else if (obj.fileNames != null) {  //File da nop
                    let url = "";
                    for (let i = 0; i < obj.fileNames.length; i++) {
                        url = url + "<a class='text-primary' href = '" + obj.fileLinks[i] + "' >" + obj.fileNames[i] + "</a ><br>";
                    }
                    $(".msg").html("<p></p><b>File đã nộp</b>: <br>" + url);
                }
            }
        });
    });
}

function decodeHtml(html) {
    var txt = document.createElement("textarea");
    txt.innerHTML = html;
    return txt.value;
}

let unsubscribe;

function showQuizResult(me) {
    if (unsubscribe != null) {
        unsubscribe();
    }
    let temp = $(me).text();
    $(".show-result").text("Kết quả")
    $(me).text(temp)
    $(".user-answer-choice").html("");
    $(".user-answer-code").html("");
    let survey_id = $(me).closest("google-codelab-survey").attr('survey-id')
    if ($(me).text() === "Kết quả") {

        unsubscribe = firebase.firestore().collection("rooms").doc(getRoomID()).collection("surveys").doc(survey_id).collection("answers").onSnapshot({includeMetadataChanges: false}, (snapshot) => {
            snapshot.docChanges().forEach((change) => {
                let doc = change.doc
                let obj = change.doc.data()

                if (change.type === "added") {  //Thêm
                    $(me).parent().next().find('#' + doc.id).css('color', 'blue');
                }

                if (change.type === "modified") {  //Sửa
                    $('#submit-' + doc.id).remove()  //Xóa bài đã làm
                    $(me).parent().next().find('#' + doc.id).css('color', 'red');
                }

                if (obj.choice != null) //Nếu là câu hỏi trắc nghiệm
                    $(me).parent().next().children().eq(obj.choice).find(".user-answer-choice").append("<span id='submit-" + doc.id + "'>[" + obj.uname + "] </span>")
                else if (obj.content != null) //Nếu là câu trả lời dài
                    $(me).closest("google-codelab-survey").find(".user-answer-code").append("<div id='submit-" + doc.id + "'>" + obj.uname + ": <pre><code>" + obj.content + "</code></pre></div>");
                else if (obj.fileLinks != null) {  //Nếu là upload file
                    let url = "";
                    for (let i = 0; i < obj.fileLinks.length; i++) {
                        url = url + "<a class='text-primary' href = '" + obj.fileLinks[i] + "' >" + obj.fileNames[i] + "</a > ";
                    }
                    $(me).closest("google-codelab-survey").find(".user-answer-file").append("<div id='submit-" + doc.id + "'>" + obj.uname + ": " + url + "</div>");
                }

                if (change.type === "removed") { //Xóa???
                    console.log("Removed city: ", change.doc.data());
                }


                timer = setTimeout(function () {
                    $(me).parent().next().find('#' + doc.id).css('color', '');
                }, 2000);
            });

            hljs.highlightAll();
            addCopyButtons(navigator.clipboard)
            $("#submit-spinner").addClass("d-none");
        });
        $(me).text("Ẩn")
    } else {

        unsubscribe();
        $(me).text("Kết quả");
    }


}

function realtime(user) {
    //Check realtime
    $('#main').show();
    $('#drawer').show();
    refUsers = firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/users');
    refUsers.on('value', (snapshot) => {  //Khi có bất kỳ sự thay đổi trong labs/docID_ABC/room_123/users
        const data = snapshot.val();
        let count = []
        let totalUser = 0;
        $('#usersChat').empty()
        for (let uid in data) {
            let step = data[uid].step;
            if (count[step] == undefined)
                count[step] = {count: 0, user: ""};
            count[step].count++;
            count[step].user = count[step].user + data[uid].name + "<br>";
            totalUser++;

            if (teacher) {  //For Teacher only
                $('[id^=' + uid + ']').removeClass("yellow")
                if (data[uid].isRaise) {
                    $("#" + uid + "_" + step).addClass("yellow")
                }
            }

            //Add to chat room
            // if (currentUser.uid != uid) {  //Kh
            let avatar = "<img src='" + data[uid].photo + "' alt='user' width='40' height='40'  class='rounded-circle'>";
            if (data[uid].photo !== undefined) {
                avatar = "<img src='" + data[uid].photo + "' alt='user' width='40' height='40'  class='rounded-circle'>";
            } else {
                avatar = "<img src='/images/user.svg' alt='user' width='40' height='40'  class='rounded-circle'>";
            }
            $('#usersChat').append("<a id='chat" + uid + "' href='#' onclick='showChat(this,\"" + uid + "\")' class='px-2 list-group-item list-group-item-action rounded-0 media uchat'>" + avatar + "<div class='media-body'>" + data[uid].name + "</div></a>")


            //Update in Chat room
            if (data[uid].isRaise) {
                $("#chat" + uid).addClass("yellow")
            } else {
                $("#chat" + uid).removeClass("yellow")
            }
        }

        for (let i = 1; i <= getNumberOfSteps(); i++) {
            if (count[i - 1] == undefined)
                $('li:nth-child(' + i + ') > a > span.badge').addClass("invisible")
            else {
                $('li:nth-child(' + i + ') > a > span.badge').removeClass("invisible")
                $('li:nth-child(' + i + ') > a > span.badge').text(count[i - 1].count);
                $('li:nth-child(' + i + ') > a > span.badge').attr("user", count[i - 1].user)
            }
        }

        $('#numOnline').text(totalUser)
    });

    updateStep(getSelectedStep());
    //Listen to Notification
    let first = true;
    firebase.database().ref('/notifies/' + currentUser.uid).on('value', (snapshot) => {
        if (!first) {
            const data = snapshot.val();
            if (!$("#collapse-online").hasClass("show") || ($("#collapse-online").hasClass("show") && sendTo !== data.uid)) {
                showToast(data)
            }
        }
        first = false;
    });

    let firstAll = true;  //Nghe toàn bộ sự kiện gửi đến phòng
    firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/notifies/all').on('value', (snapshot) => {
        if (!firstAll) {
            if (!$("#collapse-online").hasClass("show") || ($("#collapse-online").hasClass("show") && sendTo !== "all")) {
                const data = snapshot.val();
                showToast(data);
            }
        }
        firstAll = false;
    });
    // refUsers = firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/users');

    //OnDisconnect
    let leave = {};
    leave[currentUser.uid] = null;
    refUsers.onDisconnect().update(leave)

    let leave_notify = {
        uid: currentUser.uid,
        uname: currentUser.displayName,
        time: firebase.database.ServerValue.TIMESTAMP,
        type: TOAST_LEAVE_ROOM
    }

    firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/notifies/all').onDisconnect().update(leave_notify);
    let enter_notify = {
        uid: currentUser.uid,
        uname: currentUser.displayName,
        time: firebase.database.ServerValue.TIMESTAMP,
        type: TOAST_ENTER_ROOM
    }
    firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/notifies/all').onDisconnect().set(enter_notify);

    let enter = {};
    let curStep = new URL(window.location.href).hash.split("#")[1];
    if (!curStep)
        curStep = -1;

    enter[user.uid] = {
        step: curStep,
        time: firebase.database.ServerValue.TIMESTAMP,
        name: $("#name").text(),
        photo: user.photoURL
    };

    firebase.database().ref('.info/connected').on('value', function (snapshot) {
        if (snapshot.val() == false) {
            //When user if offline
            return;
        }
        refUsers.onDisconnect().update(leave).then(function () {
            refUsers.update(enter)
        });

        firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/notifies/all').onDisconnect().set(leave_notify).then(function () {
            firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/notifies/all').set(enter_notify)
        });
    });
}

function logoutRoom() {
    let leave = {};
    leave[currentUser.uid] = null;
    refUsers.update(leave);
    //Ra khoi phong

    sendNotify("all", null, TOAST_LEAVE_ROOM);


    $('#main').hide();
    $('#drawer').hide();
    $('.room').addClass("d-none")
    if (refUsers)
        refUsers.off();
    if (refChat)
        refChat.off();
}

function showChat(me, uid) {
    sendTo = uid;
    if (refChat != null)
        refChat.off()
    $('#chatMessages').empty();
    if (uid === "all") {
        refChat = firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/chats/all/');
        chatroom = uid;
    } else {
        if (uid > currentUser.uid)
            chatroom = uid + "-" + currentUser.uid;
        else
            chatroom = currentUser.uid + "-" + uid
        refChat = firebase.database().ref('/chats/' + chatroom);  //Private chat
    }
    refChat.on('child_added', (data) => {
        showMessage(data.val());
    });
    $(".uchat").removeClass("active text-white")
    $(me).addClass("active text-white")
}

function sendMessage() {
    if ($('#txtMessage').val().trim() !== "") {
        let change = {};
        change[refChat.push().key] = {
            uid: currentUser.uid,
            name: currentUser.displayName,
            photo: currentUser.photoURL,
            time: firebase.database.ServerValue.TIMESTAMP,
            message: $('#txtMessage').val()
        };
        refChat.update(change);
        sendNotify(sendTo, $('#txtMessage').val(), TOAST_CHAT_ROOM)
        $('#txtMessage').val("")
        //REMOVE OLD CHAT
        const MAX_COUNT = 99;  //Keep 100 recent
        refChat.once('value', function (snapshot) {
            if (snapshot.numChildren() > MAX_COUNT) {
                let childCount = 0;
                let updates = {};
                snapshot.forEach(function (child) {
                    if (++childCount < snapshot.numChildren() - MAX_COUNT) {
                        updates[child.key] = null;
                    }
                });
                refChat.update(updates);
            }
        });
    }
}

function sendNotify(sendTo, message, type) {
    let ref;
    if (sendTo === "all") {
        ref = firebase.database().ref('/labs/' + currentDocID + '/' + getRoomID() + '/notifies/all')
    } else {
        ref = firebase.database().ref('/notifies/' + sendTo)
    }
    ref.set({
        uid: currentUser.uid,
        uname: currentUser.displayName,
        message: message,
        time: firebase.database.ServerValue.TIMESTAMP,
        type: type
    });
}

function showMessage(data) {
    if (currentUser.uid === data.uid)
        $('#chatMessages').append("<div class='ml-auto d-flex justify-content-end'><div class='pt-2 chat-body'><div class='bg-primary rounded-pill py-2 px-3  text-white text-small'>" + data.message + "</div><span class='text-muted d-flex justify-content-end chat-time'>" + time_ago(data.time) + "</span></div></div>\n")
    else {

        if (data.photo !== undefined) {
            avatar = "<img src='" + data.photo + "' alt='user' width='40' height='40'  class='rounded-circle'>";
        } else {
            avatar = "<img src='/images/user.svg' alt='user' width='40' height='40'  class='rounded-circle'>";
        }
        $('#chatMessages').append("<div class='media w-75 '>" + avatar + "<div class='media-body ms-1'><div class='bg-light rounded-pill py-2 px-3'><span class='text-small mb-0 text-muted'>" + data.message + "</span></div><p class='text-muted chat-time'>" + time_ago(data.time) + "</p></div></div>");
    }
    let objDiv = document.getElementById("chatMessages");
    objDiv.scrollTop = objDiv.scrollHeight;
}

function getNumberOfSteps() {
    let steps = $(".steps ol li");
    return steps.length;
}

function getSelectedStep() {
    const radioButtons = $(".steps ol li");
    for (const element of radioButtons) {
        if (element.hasAttribute("selected")) {
            return radioButtons.index(element)
        }
    }
}

function getSelectedStepText(step) {
    return $(".steps > ol > li:nth-child(" + (step + 1) + ") .step").text()
}

let oldStep = -1;
let oldTime = 0;


function updateStep(step) {  //Up to Realtime database
    if (!isNaN(step) && refUsers != null) {

        let newTime = Math.floor(Date.now() / 1000);
        let duration = newTime - oldTime;

        if (duration > 15 && duration < 1800) {
            firebase.firestore().collection("rooms").doc(getRoomID()).collection("logs").doc(currentUser.uid).collection("steps").add({
                time: firebase.firestore.FieldValue.serverTimestamp(),
                enter: step,
                leave: oldStep,
                duration: duration
            });

            firebase.firestore().collection("rooms").doc(getRoomID()).collection("logs").doc(currentUser.uid).update({
                ['s' + oldStep]: firebase.firestore.FieldValue.increment(duration),
                lastAction: firebase.firestore.FieldValue.serverTimestamp()
            });
        }

        if (currentUser != null) {
            let change = {};
            change[currentUser.uid] = {
                step: step,
                time: firebase.database.ServerValue.TIMESTAMP,
                name: $("#name").text(),
                photo: currentUser.photoURL
            };

            if (raiseHand) {
                change[currentUser.uid].isRaise = true;
            }
            refUsers.update(change);
        }
        oldStep = step
        oldTime = Math.floor(Date.now() / 1000);

        //Load Submitted
    }
}

function hoverDiv(e, state) {
    if (state === 1) {
        let left = 40 + $(e).offset().left + "px";
        let top = $(e).offset().top + "px";
        let div = document.getElementById('divtoshow');
        div.innerHTML = $(e).attr("user")
        div.style.left = left;
        div.style.top = top;
        $("#divtoshow").show();
    } else {
        $("#divtoshow").hide();
    }
}

function getDocID() {
    // return "lXW9wS"; //TODO test  //lab thử nghiệm https://docs.google.com/document/d/1EEGARIc9dEj9mpnmKoYP8n4EA9KNH9qR0W2c6CYEWT0/edit#
    // return (new URL(window.location.href)).searchParams.get('room')
    let arr = (new URL(window.location.href)).pathname.split("/");
    return arr[arr.length - 1]
}

function getRoomID() {
    // return "lXW9wS"; //TODO test  //lab thử nghiệm https://docs.google.com/document/d/1EEGARIc9dEj9mpnmKoYP8n4EA9KNH9qR0W2c6CYEWT0/edit#
    // return (new URL(window.location.href)).searchParams.get('room')
    let arr = (new URL(window.location.href)).pathname.split("/");
    return arr[arr.length - 1]
}

function deleteUserReport(userID) {  //Hàm này có dùng nhé không được xóa
    let room = {}
    $("#tr-report-" + userID).remove();
    room["roomID"] = getRoomID();
    room["userID"] = userID;
    $.ajax({
        url: "/deleteUserReport",
        type: "POST",
        data: JSON.stringify(room),
        dataType: "json",
        contentType: "application/json",
        success: function (data) {
        },
        error: function (e) {
        }
    })
}

function updateHTML() {
    let lab = {}
    lab["docID"] = currentDocID;

    $.ajax({  //Chuyển Request tới server
        url: "/createLab?action=updateHTML",
        type: "POST",
        data: JSON.stringify(lab),
        dataType: "json",
        contentType: "application/json",
        success: function (lab) {

            let data = {}
            $("lab").html(lab.html)
            data.uname = "Hệ thống"
            data.message = "Cập nhật thành công"
            data.type = TOAST_CHAT_ROOM
            topButton.appendTo("#codelab-title");
            mofifyLab();
            showToast(data)
        },
        error: function (e) {
            let data = {}
            data.uname = "Hệ thống"
            data.message = "Cập nhật thất bại"
            data.type = TOAST_CHAT_ROOM
            showToast(data)
        }
    })
};


let topButton;

function addCopyButtons(clipboard) {
    document.querySelectorAll(".slide pre > code").forEach(function (codeBlock) {
        let pre = codeBlock.parentNode;
        $(pre).wrap("<div class='block-code'></div>");
        let button = document.createElement("a");
        button.className = "btn btn-success btn-right-corner";
        button.innerText = "Copy";
        button.addEventListener("click", function () {
            clipboard.writeText(codeBlock.textContent).then(function () {
                button.blur();
                button.innerText = "Copied!";
                setTimeout(function () {
                    button.innerText = "Copy"
                }, 2e3)
            }, function (error) {
                button.innerText = "Error";
                console.error(error)
            })
        });
        $(pre).before(button);
    })
}

function updateAnswer(survey_id) {
    firebase.firestore().collection("rooms").doc(getRoomID()).collection("surveys").doc(survey_id).set({  //Lưu vào array
        answers: firebase.firestore.FieldValue.arrayUnion(currentUser.uid + " - " + currentUser.displayName)
    }, {merge: true})
}

function mofifyLab() {
    $(".slide aside").on('click', function (ev) {
        // let me = ev.currentTarget;
        // $(me).
        $(this).children("p").toggle();
        $(this).children("span").toggle();
    })
    $(".slide .inner > table").wrap("<div class='table-lab'></div>");
    $('.slide .inner .table-lab table:has(tr:eq(0):last-child)').addClass('table-onerow');

    $('.slide .inner .table-onerow td').addClass('align-middle');

    $('.slide .inner .table-lab table:has(tr:not(:eq(0):last-child))').addClass("table table-striped table-bordered")

    $('span.option-text').closest("label.survey-option-wrapper").each(function () {
        let text = $(this).text()
        let input = $(this).find("input")

        $(this).replaceWith("<div class='form-check'>" +
            "  <input class='form-check-input' type='radio' name='" + input.attr("name") + "' id='" + input.attr("id") + "'>" +
            "  <label class='form-check-label' for='" + input.attr("id") + "'>" +
            text +
            "  </label> <span class='user-answer-choice'></span>" +
            "</div>")
    })


    $('label.form-check-label:contains("Code"),label.form-check-label:contains("Mã nguồn")').closest(".survey-question-options").replaceWith("" +
        "<div class='container-code'>" +
        "    <textarea rows='10' class='textarea-code'></textarea>       " +
        "    <a href='#' class='btn btn-success btn-upload-code btn-right-corner'>Lưu</a>" +
        "</div><div class='user-answer-code'></div>")

    $('label.form-check-label:contains("Text"),label.form-check-label:contains("Văn bản")').closest(".survey-question-options").replaceWith("" +
        "<div class='container-code'>" +
        "    <textarea rows='10' class='textarea-text'></textarea>       " +
        "    <a href='#' class='btn btn-success btn-upload-code btn-right-corner'>Lưu</a>" +
        "</div>")


    $('label.form-check-label:contains("File"),label.form-check-label:contains("Files")').closest(".survey-question-options").replaceWith("" +
        "<div class='container-code'>" +
        "    <form method='post' enctype='multipart/form-data'>" +
        "       <input type='file' name='files' multiple>       " +
        "       <a href='#' class='btn btn-success btn_upload_file btn-right-corner'>Lưu</a>" +
        "       <div class='msg flex-grow-1'></div>" +
        "   </form>" +
        "</div><div class='user-answer-file'></div>")

    //Youtube
    $("p:contains('https://www.youtube.com/watch?v=')").each(function () {
        let url = new URL($(this).text());
        $(this).html('<div class="youtube-container"><iframe src="https://www.youtube.com/embed/' + url.searchParams.get("v") + '" title="YouTube video player"  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowFullScreen class="video"></iframe>')
    })


    $("p:contains('https://codepen')").each(function () {
        let url = new URL($(this).text());
        let arr = url.pathname.split("/");

        $(this).html('<p class="codepen" data-height="600" data-default-tab="html,result" data-slug-hash="' + arr[arr.length - 1] + '" data-preview="true" data-editable="true"\n' +
            '   style="height: 600px; box-sizing: border-box; display: flex; align-items: center; justify-content: center; border: 2px solid; margin: 1em 0; padding: 1em;">\n' +
            '</p>');
    })


    addCopyButtons(navigator.clipboard)
    $(".contentInput").text("")
    $(".msg").html("")
    $('span.option-text:contains("File")').closest("label.survey-option-wrapper").html("")
    $('.survey-option-wrapper').append("<span class='user-answer-choice'></span>")
    $(".form-check-input").on('click', function (ev) { //Quiz
        let me = ev.currentTarget;
        let survey_id = $(me).closest("google-codelab-survey").attr('survey-id')
        let choice = $(me).closest(".survey-question-options").find(".form-check-input").index($(me));
        firebase.firestore().collection("rooms").doc(getRoomID()).collection("surveys").doc(survey_id).collection("answers").doc(currentUser.uid).set({
            uname: currentUser.displayName,
            time: firebase.firestore.FieldValue.serverTimestamp(),
            choice: choice
        })

        $('header script').remove()
        updateAnswer(survey_id)
    })


    $(".btn-upload-code").on('click', function (ev) {  //Submit code
        let me = ev.currentTarget;
        $(me).addClass("d-none")
        let survey_id = $(me).closest("google-codelab-survey").attr('survey-id')
        let content = $(me).prev().val()
        firebase.firestore().collection("rooms").doc(getRoomID()).collection("surveys").doc(survey_id).collection("answers").doc(currentUser.uid).set({
            uname: currentUser.displayName,
            time: firebase.firestore.FieldValue.serverTimestamp(),
            content: $('<textarea/>').text(content).html()
        }).then(function () {
            $(me).removeClass("d-none")
        })

        updateAnswer(survey_id)

        return true;
    })

    $(".btn_upload_file").on('click', function (ev) {  //Upload file
        let me = $(ev.currentTarget);

        $(me).addClass("d-none")
        let survey_id = $(me).closest("google-codelab-survey").attr('survey-id')
        let form = me.closest("form")
        let msg = form.find(".msg").first()
        $(".upload-spinner").removeClass("d-none");
        $(".upload-form").addClass("d-none");
        $(".btn_upload").addClass("d-none");
        let formData = new FormData(form[0]);
        formData.append("userID", currentUser.uid);
        formData.append("uname", currentUser.displayName);
        formData.append("survey_id", survey_id);
        formData.append("room", getRoomID());

        $.ajax({
            url: '/upload',
            type: 'post',
            data: formData,
            contentType: false,
            processData: false,
            success: function (response) {
                $(".upload-spinner").addClass("d-none");
                form.removeClass("d-none");
                msg.html(response);
                form.trigger("reset");
                me.removeClass("d-none");
                updateAnswer(survey_id)
            },
            error: function () {
                msg.html("Có lỗi xảy ra!");
                $(".upload-spinner").addClass("d-none");
                form.removeClass("d-none");
                form.trigger("reset");
                me.removeClass("d-none");
            }
        });
    })


}

$(function () {
    // initialize and show Bootstrap 4 toast

    page = "lab";
    $('#drawer .metadata').remove();
    topButton = $("#topButton").detach()

    topButton.appendTo("#codelab-title");
    $("#creatroom").click(function () {
        $('#exampleModal').modal('show')
    });

    $("#done").hide();

    $('.steps ol li').click(function (e) {
        if (updateStep != null)
            updateStep($(this).index());
    });

    let firstEnterRoom = true
    $('#btnRoom').click(function () {
        if (firstEnterRoom) {
            showChat($('#chat0'), "all")
            firstEnterRoom = false;
        }
    })

    $('#btnRaiseHand').click(function () {
        raiseHand = $("#btnRaiseHand").hasClass("active");
        let curStep = new URL(window.location.href).hash.split("#")[1];
        updateStep(Number(curStep))  //Update

        //Ghi log vao FireStore
        if (raiseHand) {
            sendNotify("all", null, TOAST_RAISE_HAND);
            firebase.firestore().collection("rooms").doc(getRoomID()).collection("logs").doc(currentUser.uid).collection("hands").add({
                time: firebase.firestore.FieldValue.serverTimestamp(),
                type: HAND_UP,
                step: Number(curStep)
            })
            firebase.firestore().collection("rooms").doc(getRoomID()).collection("logs").doc(currentUser.uid).update({
                ['h' + oldStep]: firebase.firestore.FieldValue.increment(1),
                lastAction: firebase.firestore.FieldValue.serverTimestamp(),
            });
        } else {
            firebase.firestore().collection("rooms").doc(getRoomID()).collection("logs").doc(currentUser.uid).collection("hands").add({
                time: firebase.firestore.FieldValue.serverTimestamp(),
                type: HAND_DOWN,
                step: Number(curStep)
            })
        }
    })


    $('.steps ol li a').append("<span class='badge badge-secondary bg-secondary my-badge invisible' onmouseover='hoverDiv(this,1)' onmouseout='hoverDiv(this,0)'>0</span>")
    $('#btnLogin').hide()
    $("#next-step").click(function () {
        let curStep = new URL(window.location.href).hash.split("#")[1];
        updateStep(Number(curStep))
    });
    $("#previous-step").click(function () {
        let curStep = new URL(window.location.href).hash.split("#")[1];
        updateStep(Number(curStep))
    });

    $("#btnReport").click(function (ev) {
        if (firstReport) {
            $("#practice-tab").click();
            firstReport = false;
        } else {
            $("#" + $('.nav-tabs .active').attr("id")).click();
        }
    });

    $("#raisehand-tab").click(function (ev) {
        $("#tbody-report-raisehand").html("")
        $(".spinner-border").removeClass("d-none");
        firebase.firestore().collection("rooms").doc(getRoomID()).collection("logs").onSnapshot((querySnapshot) => {
            querySnapshot.forEach((doc) => {
                let obj = doc.data();
                let s = "";
                if (obj.lastAction != null)
                    s = s + "<td class='font14'>" + time_ago(obj.lastAction.toDate()) + "</td>";
                else
                    s = s + "<td class='font14'>" + time_ago(obj.lastEnter.toDate()) + "</td>";
                let step = "";
                for (let i = 0; i < getNumberOfSteps(); i++) {
                    if (obj["h" + i] != null) {
                        step = step + "<td class='tdcenter'><span class ='labStep blue' id=" + doc.id + "_" + i + ">" + (i + 1) + "</span><br><span class='report-detail d-none'>" + obj["h" + i] + "</span></td>";
                    } else {
                        step = step + "<td class='tdcenter'><span class ='labStep' id=" + doc.id + "_" + i + ">" + (i + 1) + "</span><br><span class='report-detail d-none'>0</span></td>";
                    }
                }
                s = s + step;
                let tdThreeDots = "<td class='text-right align-middle'><a href='#' class='bi bi-three-dots-vertical' data-bs-toggle='dropdown'></a> <div class='dropdown-menu'><a class='dropdown-item' href='#' onclick='deleteUserReport(\"" + doc.id + "\")'>Xóa</a> </div></td>";
                $("#tbody-report-raisehand").append("<tr  id='tr-report-" + doc.id + "'><td class='user-name font14'>" + obj.userName + " <span class='report-detail d-none'><br>" + obj.email + "</span></td>" + s + tdThreeDots + "</tr>")

            });

            $("#table-report-raisehand").removeClass("d-none");
            if ($('#switch-showdetail').is(':checked')) {
                $(".report-detail").removeClass("d-none")
            } else {
                $(".report-detail").addClass("d-none")
            }
            $(".spinner-border").addClass("d-none");

        });
    });

    $("#practice-tab").click(function (ev) {
        $("#tbody-report-practice").html("")
        $(".spinner-border").removeClass("d-none");
        firebase.firestore().collection("rooms").doc(getRoomID()).collection("logs").onSnapshot((querySnapshot) => {
            querySnapshot.forEach((doc) => {
                let obj = doc.data();
                let s = "";
                if (obj.lastAction != null)
                    s = s + "<td class='font14'>" + time_ago(obj.lastAction.toDate()) + "</td>";
                else
                    s = s + "<td class='font14'>" + time_ago(obj.lastEnter.toDate()) + "</td>";
                let step = "";
                for (let i = 0; i < getNumberOfSteps(); i++) {
                    if (obj["s" + i] != null) {
                        if (obj["s" + i] > 20 * 60) {  //20 min
                            step = step + "<td class='tdcenter'><span class ='labStep blue labStepSize3' id=" + doc.id + "_" + i + ">" + (i + 1) + "</span><br><span class='report-detail d-none'>" + obj["s" + i] + "s</span></td>";
                        } else if (obj["s" + i] > 10 * 60) {  //10min
                            step = step + "<td class='tdcenter'><span class ='labStep blue labStepSize2' id=" + doc.id + "_" + i + ">" + (i + 1) + "</span><br><span class='report-detail d-none'>" + obj["s" + i] + "s</span></td>";
                        } else if (obj["s" + i] > 5 * 60) {  //5 min
                            step = step + "<td class='tdcenter'><span class ='labStep blue labStepSize1' id=" + doc.id + "_" + i + ">" + (i + 1) + "</span><br><span class='report-detail d-none'>" + obj["s" + i] + "s</span></td>";
                        } else if (obj["s" + i] > 15) {  //15 sec
                            step = step + "<td class='tdcenter'><span class ='labStep blue' id=" + doc.id + "_" + i + ">" + (i + 1) + "</span><br><span class='report-detail d-none'>" + obj["s" + i] + "s</span></td>";
                        }
                    } else {
                        step = step + "<td class='tdcenter'><span class ='labStep' id=" + doc.id + "_" + i + ">" + (i + 1) + "</span><br><span class='report-detail d-none'>0s</span></td>";
                    }
                }
                s = s + step;
                let tdThreeDots = "<td class='text-right align-middle'><a href='#' class='bi bi-three-dots-vertical' data-bs-toggle='dropdown'></a> <div class='dropdown-menu'><a class='dropdown-item' href='#' onclick='deleteUserReport(\"" + doc.id + "\")'>Xóa</a> </div></td>";
                $("#tbody-report-practice").append("<tr  id='tr-report-" + doc.id + "'><td class='user-name font14'>" + obj.userName + " <span class='report-detail d-none'><br>" + obj.email + "</span></td>" + s + tdThreeDots + "</tr>")
                $("#table-report-practice").removeClass("d-none");
            });
            if ($('#switch-showdetail').is(':checked')) {
                $(".report-detail").removeClass("d-none")
            } else {
                $(".report-detail").addClass("d-none")
            }
            $("#practice-spinner").addClass("d-none");
        });
    });

    $("#submit-tab").click(function (ev) {

        firebase.firestore().collection("rooms").doc(getRoomID()).collection("surveys").onSnapshot((querySnapshot) => {
            $("#tbody-report-submit").html("")
            $("#table-report-submit").removeClass("d-none");

            let map = new Map();
            let ex = []

            querySnapshot.forEach((doc) => { //Duyet tung nguoi dung
                let obj = doc.data();
                ex.push(doc.id)
                let s = "";
                if (obj.answers != null) {
                    s = s + "<td>";
                    for (let i = 0; i < obj.answers.length; i++) {
                        let a = map.get(obj.answers[i]);
                        if (a == null)
                            a = [doc.id]
                        else
                            a.push(doc.id)
                        map.set(obj.answers[i], a)
                    }
                    s = s + "</td>"
                } else {
                    s = s + "<td></td>";
                }

            });


            for (const [key, value] of map) {
                let id = key.split("-")[0].trim()
                let name = key.split("-")[1].trim()
                let step = ""
                for (let i = 0; i < ex.length; i++) {
                    let arr = ex[i].split("-")
                    let ex_id = arr[arr.length - 1]
                    if (value.includes(ex[i]))
                        step = step + "<td class='tdcenter'><span class ='labStep blue' id=" + id + "_" + i + ">" + ex_id + "</span><br></td>";
                    else
                        step = step + "<td class='tdcenter'><span class ='labStep' id=" + id + "_" + i + ">" + ex_id + "</span><br></td>";
                }
                let tdThreeDots = "<td class='text-right align-middle'><a href='#' class='bi bi-three-dots-vertical' data-bs-toggle='dropdown'></a> <div class='dropdown-menu'><a class='dropdown-item' href='#' onclick='deleteUserReport(\"" + id + "\")'>Xóa</a> </div></td>";
                $("#tbody-report-submit").append("<tr  id='tr-report-" + id + "'><td class='user-name'>" + name + "</td>" + step + tdThreeDots + "</tr>")
            }


            if ($('#switch-showdetail').is(':checked')) {
                $(".report-detail").removeClass("d-none")
            } else {
                $(".report-detail").addClass("d-none")
            }

            $("#submit-spinner").addClass("d-none");
        });


    });

    $('#switch-showdetail').click(function (e) {
        if ($('#switch-showdetail').is(':checked')) {
            $(".report-detail").removeClass("d-none")
        } else {
            $(".report-detail").addClass("d-none")
        }
    });

    $(document).on('keydown', function (e) {
        if (e.keyCode === 37 || e.keyCode === 39) {
            // let curStep = new URL(window.location.href).hash.split("#")[1];
            // updateStep(Number(curStep))
            // e.preventDefault()
            // e.stopPropagation();
        }
    });

    $("google-codelab-step .instructions").append("<div class='extend'></div>");

    // $("#main").after("<div id = 'chat'>abc</div>")
    //Modify HTML Lab
    mofifyLab()

    $('#txtMessage').keypress(function (e) {
        if (e.which == 13) {
            sendMessage()
            return false;    //<---- Add this line
        }
    });

});


