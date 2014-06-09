package pt.com.broker.client.nio.android.test;

import android.os.Bundle;
import android.test.InstrumentationTestRunner;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the SAPO nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 09-06-2014.
 */
public class CustomInstrumentationTestRunner extends com.uphyca.testing.JUnit4InstrumentationTestRunner {

    @Override public void onCreate (final Bundle arguments) {
        super.onCreate(arguments);

        // temporary workaround for an incompatibility in current dexmaker (1.1) implementation and Android >= 4.3
        // cf. https://code.google.com/p/dexmaker/issues/detail?id=2 for details
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }
}
