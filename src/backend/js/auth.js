import {
    database,
    firebaseConfig
} from './database.js';
// var database = firebase.database();
// Initialize Firebase
// firebase.initializeApp(firebaseConfig);
var auth = firebase.auth();

// Initialize the FirebaseUI Widget using Firebase.
var ui = new firebaseui.auth.AuthUI(auth);

var uiConfig = {
    callbacks: {
        signInSuccessWithAuthResult: function (authResult, redirectUrl) {
            // User successfully signed in.
            // Return type determines whether we continue the redirect automatically
            // or whether we leave that to developer to handle.

            if (authResult.additionalUserInfo.isNewUser) {
                let userId = auth.currentUser.uid;

                firebase.database().ref('users/' + userId).set({
                    "uid": userId,
                    "displayName": auth.currentUser.displayName,
                    "email": auth.currentUser.email,
                    "civilEngineer": false,
                    "photoUrl": auth.currentUser.photoURL,
                    "registered": parseInt(auth.currentUser.metadata.b)
                }).then((data) => {
                    window.location.href="./map.html";
                });
                
            } else {
                window.location.href="./map.html";
            }
            return false;

        },
        uiShown: function () {
            // The widget is rendered.
            // Hide the loader.

        }
    },
    // Will use popup for IDP Providers sign-in flow instead of the default, redirect.
    signInFlow: 'popup',
    signInOptions: [
        // Leave the lines as is for the providers you want to offer your users.
        firebase.auth.GoogleAuthProvider.PROVIDER_ID,
        {
            provider: firebase.auth.EmailAuthProvider.PROVIDER_ID,
            requireDisplayName: true
        }
    ],
    // Terms of service url.
    tosUrl: '<your-tos-url>',
    // Privacy policy url.
    privacyPolicyUrl: '<your-privacy-policy-url>'
};

// The start method will wait until the DOM is loaded.
ui.start('#firebaseui-auth-container', uiConfig);

function signOut() {
    // [START auth_sign_out]
    firebase.auth().signOut().then(() => {
        // Sign-out successful.
    }).catch((error) => {
        // An error happened.
    });
    // [END auth_sign_out]
}


export {
    uiConfig,
    signOut,
};