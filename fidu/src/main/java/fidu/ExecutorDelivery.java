/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fidu;

import android.os.Handler;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * Delivers responses and errors. Modified from Volley.
 */
public class ExecutorDelivery implements ResponseDelivery {
    /**
     * Used for posting responses, typically to the main thread.
     */
    private final Executor mResponsePoster;

    /**
     * Creates a new response delivery interface.
     *
     * @param handler {@link Handler} to post responses on
     */
    public ExecutorDelivery(final Handler handler) {
        // Make an Executor that just wraps the handler.
        mResponsePoster = new Executor() {
            @Override
            public void execute(Runnable command) {
//                FiDuLog.d("execute", Thread.currentThread().toString());
//                FiDuLog.d("execute", "" + (Looper.myLooper() == Looper.getMainLooper()));

                handler.post(command);
            }
        };
    }

    @Override
    public void postResponse(Call call, Response response, FiDuCallback callback) {
        mResponsePoster.execute(new ResponseDeliveryRunnable(null, call, response, null, callback));
    }

    @Override
    public void postFailure(Call call, Request request, IOException e, FiDuCallback callback) {
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, call, null, e, callback));
    }

    @Override
    public void postProgress(int progress, FiDuCallback callback) {
        mResponsePoster.execute(new ProgressDeliveryRunnable(progress, callback));
    }

    /**
     * A Runnable used for delivering network responses to a listener on the
     * main thread.
     */
    @SuppressWarnings("rawtypes")
    private class ResponseDeliveryRunnable implements Runnable {
        private final Call mCall;
        private final Request mRequest;
        private final Response mResponse;
        private final FiDuCallback mCallback;
        private final Exception mException;

        public ResponseDeliveryRunnable(Request request, Call call, Response response, Exception
                exception, FiDuCallback callback) {
            mCall = call;
            mRequest = request;
            mResponse = response;
            mCallback = callback;
            mException = exception;
        }

        @Override
        public void run() {
            if (mCall.isCanceled()) {
                return;
            }

            if (mResponse != null && mResponse.isSuccessful()) {
                mCallback.onResponse(mResponse);
            } else {
                mCallback.onFailure(mRequest, mException);
            }
        }
    }

    /**
     * A Runnable used for delivering network responses progress to a listener on the
     * main thread.
     */
    private class ProgressDeliveryRunnable implements Runnable {
        private final int mProgress;
        private final FiDuCallback mCallback;

        public ProgressDeliveryRunnable(int progress, FiDuCallback callback) {
            mProgress = progress;
            mCallback = callback;
        }

        @Override
        public void run() {
            mCallback.onProgress(mProgress);
        }
    }
}
