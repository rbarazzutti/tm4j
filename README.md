using a begin and a commit JNI methods:
Using RTM/TSX via JNI in java is not possible because the wrapper for JNI call is using a forbidden instruction (vzeroupper) at the return of the native call.

Using a callback:
Seems to work even if we get more capacity issue.

