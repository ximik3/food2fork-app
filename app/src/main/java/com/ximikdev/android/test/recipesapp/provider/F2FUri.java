package com.ximikdev.android.test.recipesapp.provider;

import android.net.Uri;

/**
 * Uri to access Food2Fork.com API
 * Content Provider's adaptation for URL queries.
 * It has the same syntax as the original API.
 * @see <a href="http://food2fork.com/about/api">food2fork.com/about/api</a>
 */
public class F2FUri {
    private static final String _TAG = F2FUri.class.getSimpleName();

    public static final String GET = F2FContentProvider.GET_PATH;
    public static final String SEARCH = F2FContentProvider.SEARCH_PATH;

    public static final String KEY = "key";
    public static final String Q = "q";
    public static final String SORT = "sort";
    public static final String RID = "rId";
    public static final String PAGE = "page";

    public static final String TRENDING = "t";
    public static final String RATING = "r";
    private static final String UNSPECIFIED = "";

    // Content uri
    private final Uri uri;

    // Action: search or get
    private final String action;
    // Key values
    private final String key;
    private final String q;
    private final String sort;
    private final String rId;
    private final int page;

    // Constructors
    public F2FUri(Uri uri) {
        this.uri = uri;
        action = uri.getLastPathSegment();
        key = uri.getQueryParameters(KEY).isEmpty() ?
                UNSPECIFIED : uri.getQueryParameters(KEY).get(0);
        q = uri.getQueryParameters(Q).isEmpty() ?
                UNSPECIFIED : uri.getQueryParameters(Q).get(0);
        sort = uri.getQueryParameters(SORT).isEmpty() ?
                UNSPECIFIED : uri.getQueryParameters(SORT).get(0);
        rId = uri.getQueryParameters(RID).isEmpty() ?
                UNSPECIFIED : uri.getQueryParameters(RID).get(0);
        page = uri.getQueryParameters(PAGE).isEmpty() ?
                0 : parseInt(uri.getQueryParameters(PAGE).get(0));
    }

    private int parseInt(String page) {
        try {
            return Integer.parseInt(page);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public F2FUri(String uri) {
        this(Uri.parse(uri));
    }

    // Getters
    public Uri getUri() {
        return uri;
    }

    public String getAction() {
        return action;
    }

    public String getKey() {
        return key;
    }

    public String getQ() {
        return q;
    }

    public String getSort() {
        return sort;
    }

    public String getRid() {
        return rId;
    }

    /**
     * @return 0 if unspecified of wrong format
     */
    public int getPage() {
        return page;
    }


    // Checkers
    public boolean hasKey() {
        return !key.isEmpty();
    }

    public boolean hasQ() {
        return !q.isEmpty();
    }

    public boolean hasSort() {
        return !sort.isEmpty();
    }

    public boolean hasRid() {
        return !rId.isEmpty();
    }

    public boolean hasPage() {
        return page > 0;
    }


    // Parsers
    public static F2FUri parse(Uri uri) {
        return new F2FUri(uri);
    }

    public static F2FUri parse(String uri) {
        return new F2FUri(uri);
    }


    @Override
    public String toString() {
        return uri.toString();
    }

    @Override
    public boolean equals(Object o) {
        F2FUri fUri;

        if (o instanceof Uri) {
            fUri = parse((Uri) o);
        } else if (o instanceof String) {
            fUri = parse((String) o);
        } else if (o instanceof F2FUri) {
            fUri = (F2FUri) o;
        } else {
            return false;
        }
        return fUri.getUri().getScheme().equals(uri.getScheme())
                && fUri.getUri().getAuthority().equals(uri.getAuthority())
                && fUri.getUri().getPath().equals(uri.getPath())
                && fUri.getKey().equals(getKey())
                && fUri.getQ().equals(getQ())
                && fUri.getSort().equals(getSort())
                && fUri.getRid().equals(getRid())
                && fUri.getPage() == getPage();
    }
}
