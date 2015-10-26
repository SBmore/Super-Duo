package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import it.jaschke.alexandria.data.AlexandriaContract;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = AddBook.class.getSimpleName();
    private static final String SCAN_TAG = "Barcode_Scanner";
    private static final String ISBN_13 = "978";
    private EditText ean;
    private String isbn;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT = "eanContent";

    public AddBook() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ean != null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();

        if (arguments != null && arguments.containsKey(BarcodeScanner.ISBN_TAG) &&
                arguments.containsKey(BarcodeScanner.SCAN_TYPE_TAG)) {
            isbn = arguments.getString(BarcodeScanner.ISBN_TAG);
            String scanType = arguments.getString(BarcodeScanner.SCAN_TYPE_TAG);

            if (scanType == BarcodeFormat.ISBN10.getName() || scanType == BarcodeFormat.ISBN13.getName()) {
                launchIsbnSearch(scanType);
            } else {
                String text = getString(R.string.unsuccessful_scan);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(getContext(), text, duration);
                toast.show();
            }
        }

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);

        // Hsiao-Lu had an issue where information would disappear if she hadn't double-checked the ISBN
        // Changing the submission to a more explicit action will give people the change to re-read
        // before submitting the ISBN
        ean.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String ean = v.getText().toString();
                String scanType;
                CharSequence text = "";
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith(ISBN_13)) {
                    scanType = BarcodeFormat.ISBN10.getName();
                } else {
                    scanType = BarcodeFormat.ISBN13.getName();
                }
                if (ean.length() < 13) {
                    text = getString(R.string.too_short_isbn);
                } else if (!ean.startsWith(ISBN_13)) {
                    text = getString(R.string.wrong_start_isbn);
                }
                if (text != "") {
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(getContext(), text, duration);
                    toast.show();
                    return false;
                }

                clearFields();
                isbn = ean;
                launchIsbnSearch(scanType);
                return true;
            }
        });

        // Josh and Lauren's feedback showed frustration that the app claims it can scan when it couldn't.
        // This functionality has now been added without the user having to install a separate app
        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide the soft keyboard whilst scanning
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                BarcodeScanner scanner = new BarcodeScanner();

                try {
                    getFragmentManager().beginTransaction()
                            .replace(((ViewGroup) getView().getParent()).getId(), scanner, SCAN_TAG)
                            .addToBackStack(null)
                            .commit();
                } catch (NullPointerException e) {
                    Log.e(e.getMessage(), LOG_TAG);
                }
            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ean.setText("");
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
            }
        });

        if (savedInstanceState != null) {
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
        }

        return rootView;
    }

    private void launchIsbnSearch(String scanType) {
        Intent bookIntent = new Intent(getActivity(), BookService.class);

        if (scanType.equals(BarcodeFormat.ISBN10.getName())) {
            isbn = fixCheckSum(ISBN_13 + isbn);
        }

        //Once we have an ISBN, start a book intent
        bookIntent.putExtra(BookService.EAN, isbn);
        bookIntent.setAction(BookService.FETCH_BOOK);
        getActivity().startService(bookIntent);
        AddBook.this.restartLoader();
    }

    // The barcode scanner will scan ISBN 13 numbers as ISBN 10 so work out what the last
    // digit should be
    private String fixCheckSum(String isbn) {
        String newIsbn = isbn.substring(0, isbn.length()-1);
        char[] allNums = newIsbn.toCharArray();
        int total = 0;

        for (int x = 0; x < newIsbn.length(); x += 1) {
            if (x % 2 == 0) {
                total += Integer.parseInt(""+allNums[x]) * 1;
            } else {
                total += Integer.parseInt(""+allNums[x]) * 3;
            }
        }

        int checksum = 0;
        for (int x = 0; x < newIsbn.length(); x += 1){
            int mod = total % 10;
            if(mod != 0){
                checksum = 10 - mod;
            }
        }

        newIsbn = newIsbn + Integer.toString(checksum);

        return newIsbn;
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(isbn)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors != null) {
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        } else {
            ((TextView) rootView.findViewById(R.id.authors)).setLines(1);
            ((TextView) rootView.findViewById(R.id.authors)).setText("No Author Information Found");
        }
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields() {
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }
}
