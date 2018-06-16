package com.indieweb.indigenous.micropub.post;

import android.view.MenuItem;

public interface SendPostInterface {

    /**
     * Onclick method that is called when hitting the send post button.
     *
     * Add extra params to send in the post request into bodyParams.
     *
     * Call sendBasePost(item); at the end to send the request.
     */
    @SuppressWarnings("unused")
    void onPostButtonClick(MenuItem item);

}
