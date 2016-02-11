package com.ximikdev.android.test.recipesapp.provider;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds uri for {@link com.ximikdev.android.test.recipesapp.MainActivity}
 */
public class F2FUriHolder {
    private static final String UNSPECIFIED = "";

    public enum Action {SEARCH, GET}

    public enum Order {RATING, TRENDING}

    private String key;
    private String q;
    private String sort;
    private String rId;
    private String page;
    private String base;
    private String action;

    private Map<String, String> args = new HashMap<>();

    public F2FUriHolder(F2FUri fUri) {
        base = fUri.getUri().getScheme() + "://" + fUri.getUri().getAuthority();
        List<String> path = fUri.getUri().getPathSegments();

        // Add path without last segment
        for (int i = 0; i < path.size() - 1; i++) {
            base += "/" + path.get(i);
        }
        // Last segment action
        action = fUri.getUri().getLastPathSegment();

        args.put(F2FUri.KEY, fUri.hasKey() ? fUri.getKey() : UNSPECIFIED);
        args.put(F2FUri.Q, fUri.hasQ() ? fUri.getQ() : UNSPECIFIED);
        args.put(F2FUri.SORT, fUri.hasSort() ? fUri.getSort() : UNSPECIFIED);
        args.put(F2FUri.RID, fUri.hasRid() ? fUri.getRid() : UNSPECIFIED);
        args.put(F2FUri.PAGE, fUri.hasPage() ? String.valueOf(fUri.getPage()) : UNSPECIFIED);
    }

    //region Setters
    public void setKey(String key) {
        args.put(F2FUri.KEY, key);
    }

    public void setQ(String q) {
        args.put(F2FUri.Q, q);
    }

    public void setSort(String sort) {
        args.put(F2FUri.SORT, sort);
    }

    public void setSort(Order sort) {
        switch (sort) {
            case RATING:
                args.put(F2FUri.SORT, F2FUri.RATING);
                break;
            case TRENDING:
                args.put(F2FUri.SORT, F2FUri.TRENDING);
                break;
        }
    }

    public void setRid(String rId) {
        args.put(F2FUri.RID, rId);
    }

    public void setPage(String page) {
        args.put(F2FUri.PAGE, page);
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setAction(Action action) {
        switch (action) {
            case SEARCH:
                this.action = F2FUri.SEARCH;
                break;
            case GET:
                this.action = F2FUri.GET;
                break;
        }
    }
    //endregion

    /**
     * Checks if Uri contain query 'keyword'
     * (e.g. http://site.com?q=smth&keyword=true&... has a 'keyword')
     *
     * @param keyword query keyword
     * @return true if contains, false otherwise
     */
    public boolean hasArgument(String keyword) {
        return args.containsKey(keyword) && !args.get(keyword).isEmpty();
    }

    @Override
    public String toString() {
        String result = base + "/" + action;

        StringBuilder query = new StringBuilder();
        for (String arg : args.keySet()) {
            if (!args.get(arg).equals(UNSPECIFIED)) {
                query.append(query.length() == 0 ? "?" : "&");
                query.append(arg);
                query.append("=");
                query.append(args.get(arg));
            }
        }
        return result + query;
    }

    public Uri toUri() {
        return Uri.parse(toString());
    }

    /**
     * {@link Uri} to {@link URL} converter
     *
     * @param uri     current Uri string (e.g. content://com.site.provider/search?q=work&r=20)
     * @param uriBase Uri basement (e.g. content://com.site.provider)
     * @param urlBase URL basement to replace (e.g. http://site.com)
     * @return URL similar to the input Uri (e.g. http://site.com/search?q=work&r=20)
     * @throws MalformedURLException
     */
    public static URL uriToUrl(String uri, String uriBase, String urlBase)
            throws MalformedURLException {
        return new URL(
                urlBase + uri.substring(uriBase.length(), uri.length())
        );
    }

    /**
     * {@link URL} to {@link Uri} converter
     *
     * @param url     current URL string (e.g. http://site.com/search?q=work&r=20)
     * @param urlBase URL basement (e.g. http://site.com)
     * @param uriBase Uri basement to replace (e.g. content://com.site.provider)
     * @return Uri similar to the input URL (e.g. content://com.site.provider/search?q=work&r=20)
     */
    public static Uri UrlToUri(String url, String urlBase, String uriBase) {
        return Uri.parse(
                uriBase + url.substring(urlBase.length(), url.length())
        );
    }
}
