var firebaseConfig = {
    apiKey: "AIzaSyCAowCDyHC5b0HhhIBvxVqc0o3lLSMXnJM",
    authDomain: "vncodelab2.firebaseapp.com",
    databaseURL: "https://vncodelab2-default-rtdb.firebaseio.com",
    projectId: "vncodelab2",
    storageBucket: "vncodelab2.appspot.com",
    messagingSenderId: "852532707206",
    appId: "1:852532707206:web:5281cd31d29828fbc7f607",
    measurementId: "G-6ZVL24X18C"
};
firebase.initializeApp(firebaseConfig);
var currentUser;
var page = "";
$(function () {
    $('#btnLogin').click(function (e) {
        openLoginModal();
    });
    $('a#google_login').click(function (e) {
        var provider = new firebase.auth.GoogleAuthProvider();
        firebase.auth()
            .signInWithPopup(provider)
            .then((result) => {
                currentUser = user;
                presence(result.user)
                afterLogin(result.user);
            }).catch((error) => {
        });
    });
    $('#signout').click(function (e) {
        logOut();
    })

    firebase.auth().onAuthStateChanged(function (user) {
        if (user) {
            currentUser = user;
            afterLogin(user);
            presence(user);

        } else {
            $('#btnLogin').removeClass("d-none")
        }
    });
});

function logOut() {
    firebase.auth().signOut().then(() => {
        $('.user').addClass("d-none");
        $('#btnLogin').click(function (e) {
            openLoginModal();
        });
        $('#btnLogin').text("Đăng nhập");
        $('#btnLogin').addClass("rounded")
        $('#profilePicture').addClass("d-none")
        $('#btnLogin').removeClass("d-none")
        $('#collapse-profile').removeClass("show")
        var userStatusDatabaseRef = firebase.database().ref('/status/' + currentUser.uid);
        var isOfflineForDatabase = {
            state: 'offline',
            last_changed: firebase.database.ServerValue.TIMESTAMP,
        };
        userStatusDatabaseRef.set(isOfflineForDatabase)
        if (refUsers) {
            logoutRoom();
        }
    });
}

function afterLogin(user) {
    $(".user").removeClass("d-none")
    $('#loginModal').modal('hide')
    $('#btnLogin').addClass("d-none")
    if (user.photoURL) {
        $('.profilePicture').attr("src", user.photoURL)
        $(".userName").hide();
    } else {
        $('.profilePicture').hide();
        $(".userName").show();
        $(".userName").text(user.displayName)
        $(".userName").nameBadge()
    }

    $('#profileName').text(user.displayName)
    $('#profileEmail').text(user.email)


    if (window.location.pathname.startsWith("/lab"))
        enterLab(user);
    else if (window.location.pathname.startsWith("/mylabs"))
        loadLabs(user);
}

function loadLabs(user) {
    var db = firebase.firestore();
    db.collection("labs").where("userID", "==", currentUser.uid)
        .get()
        .then((querySnapshot) => {
            querySnapshot.forEach((doc) => {
                var lab = "<a href='lab/" + doc.data().docID + "?room=1' class=\"codelab-card category-web\"><h2>" + doc.data().name + "</h2><h3>" + doc.data().description + "</h3><div class=\"card-footer\"><div class=\"category-icon web-icon\"></div><paper-button class=\"web-bg\">Enter</paper-button></div></a>";
                $("#cards").prepend(lab);
            });
        })
        .catch((error) => {
            console.log("Error getting documents: ", error);
        });

}

function showRegisterForm() {
    $('.loginBox').fadeOut('fast', function () {
        $('.registerBox').fadeIn('fast');
        $('.login-footer').fadeOut('fast', function () {
            $('.register-footer').fadeIn('fast');
        });
        $('.modal-title').html('Register with');
    });
    $('.error').removeClass('alert alert-danger').html('');
}

function showLoginForm() {
    $('#loginModal .registerBox').fadeOut('fast', function () {
        $('.loginBox').fadeIn('fast');
        $('.register-footer').fadeOut('fast', function () {
            $('.login-footer').fadeIn('fast');
        });

        $('.modal-title').html('Đăng nhập với');
    });
    $('.error').removeClass('alert alert-danger').html('');
}

function openLoginModal() {
    showLoginForm();
    setTimeout(function () {
        $('#loginModal').modal('show');
    }, 230);
}

function openRegisterModal() {
    showRegisterForm();
    setTimeout(function () {
        $('#loginModal').modal('show');
    }, 230);

}

function loginAjax() {
    var email = $("#email1").val();
    var password = $("#password1").val();
    firebase.auth().signInWithEmailAndPassword(email, password)
        .then((userCredential) => {
            var user = userCredential.user;
            afterLogin(user)
        })
        .catch((error) => {
            var errorMessage = error.message;
            shakeModal();
            $('.error').addClass('alert alert-danger').html(errorMessage);
        });
}

function createAccount() {
    var email = $("#email2").val();
    var password = $("#password2").val();
    firebase.auth().createUserWithEmailAndPassword(email, password)
        .then((userCredential) => {
            var user = userCredential.user;
            user.updateProfile({
                displayName: $("#name").val()
            }).then(function () {
                afterLogin(user)
            })

            // ...
        })
        .catch((error) => {
            shakeModal();
            var errorCode = error.code;
            var errorMessage = error.message;
            $('.error').addClass('alert alert-danger').html(errorMessage);
        });
}

function shakeModal() {
    $('#loginModal .modal-dialog').addClass('shake');
    $('input[type="password"]').val('');
    setTimeout(function () {
        $('#loginModal .modal-dialog').removeClass('shake');
    }, 1000);
}

function presence(user) {
    var uid = user.uid;
    var userStatusDatabaseRef = firebase.database().ref('/status/' + uid);
    var isOfflineForDatabase = {
        state: 'offline',
        last_changed: firebase.database.ServerValue.TIMESTAMP,
        uname: user.displayName
    };
    var isOnlineForDatabase = {
        state: 'online',
        last_changed: firebase.database.ServerValue.TIMESTAMP,
        uname: user.displayName
    };
    firebase.database().ref('.info/connected').on('value', function (snapshot) {
        if (snapshot.val() == false)
            return;
        userStatusDatabaseRef.onDisconnect().set(isOfflineForDatabase).then(function () {
            userStatusDatabaseRef.set(isOnlineForDatabase);
        });
    });
}

function makeid(length) {
    var result = '';
    var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for (var i = 0; i < length; i++)
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    return result;
}

function time_ago(time1) {
    time = new Date(time1);
    return TimeAgo.inWords(time.getTime());
}

var TimeAgo = (function () {
    var self = {};
    // Public Methods
    self.locales = {
        prefix: '',
        sufix: 'ago',
        seconds: 'less than a min',
        minute: 'about a minute',
        minutes: '%d minutes',
        hour: 'about an hour',
        hours: 'about %d hours',
        day: 'a day',
        days: '%d days',
        month: 'about a month',
        months: '%d months',
        year: 'about a year',
        years: '%d years'
    };

    self.inWords = function (timeAgo) {
        var seconds = Math.floor((new Date() - parseInt(timeAgo)) / 1000),
            separator = this.locales.separator || ' ',
            words = this.locales.prefix + separator,
            interval = 0,
            intervals = {
                year: seconds / 31536000,
                month: seconds / 2592000,
                day: seconds / 86400,
                hour: seconds / 3600,
                minute: seconds / 60
            };

        var distance = this.locales.seconds;

        for (var key in intervals) {
            interval = Math.floor(intervals[key]);

            if (interval > 1) {
                distance = this.locales[key + 's'];
                break;
            } else if (interval === 1) {
                distance = this.locales[key];
                break;
            }
        }

        distance = distance.replace(/%d/i, interval);
        words += distance + separator + this.locales.sufix;

        return words.trim();
    };

    return self;
}());

