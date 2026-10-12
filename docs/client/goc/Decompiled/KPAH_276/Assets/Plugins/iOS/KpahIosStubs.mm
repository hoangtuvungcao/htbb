// Plugin Objective-C goc cua TeaMobi khong co trong ban giai nen: iOSPlugins.cs khai bao cac ham
// [DllImport("__Internal")] nay, thieu thi Xcode bao "undefined symbol". Ban NQSH-H2 khong dung
// SMS / mua hang trong app nen de trong.
extern "C" {
    void _SMSsend(const char* tophone, const char* withtext, int n) {}
    int _unpause() { return 0; }
    int _checkRotation() { return 0; }
    int _back() { return 0; }
    int _Send() { return 0; }
    void _purchaseItem(const char* itemID, const char* userName, const char* gameID) {}
}
