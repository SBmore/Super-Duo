// Developed using the simple example on the repositories git hub:
// https://github.com/dm77/barcodescanner/blob/master/zbar/sample/src/main/java/me/dm7/barcodescanner/zbar/sample/SimpleScannerFragment.java

package it.jaschke.alexandria;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class BarcodeScanner extends Fragment implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;
    public static final String ADD_BOOK_TAG = "add_book";
    public static final String ISBN_TAG = "isbn";
    public static final String SCAN_TYPE_TAG = "scan_type";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mScannerView = new ZBarScannerView(getActivity());
        return mScannerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        AddBook fragment = new AddBook();
        Bundle bundle = new Bundle();
        bundle.putString(ISBN_TAG, rawResult.getContents());
        bundle.putString(SCAN_TYPE_TAG, rawResult.getBarcodeFormat().getName());
        fragment.setArguments(bundle);

        mScannerView.stopCamera();

        getFragmentManager().beginTransaction()
                .replace(((ViewGroup) getView().getParent()).getId(), fragment, ADD_BOOK_TAG)
                .addToBackStack(null)
                .commit();
    }
}