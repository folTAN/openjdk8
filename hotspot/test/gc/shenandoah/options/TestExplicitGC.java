/*
 * Copyright (c) 2017 Red Hat, Inc. and/or its affiliates.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

/*
 * @test TestExplicitGC
 * @summary Test that Shenandoah reacts to explicit GC flags appropriately
 * @key gc
 * @library /testlibrary
 * @run driver TestExplicitGC
 */

import com.oracle.java.testlibrary.*;

public class TestExplicitGC {

    enum Mode {
        PRODUCT,
        DIAGNOSTIC,
        EXPERIMENTAL,
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            System.out.println("Calling System.gc()");
            System.gc();
            return;
        }

        String[] full = new String[] {
            "Pause Full"
        };

        String[] concurrent = new String[] {
            "Pause Init Mark",
            "Pause Final Mark",
            "Pause Init Update Refs",
            "Pause Final Update Refs",
        };

        {
            ProcessBuilder pb = ProcessTools.createJavaProcessBuilder(
                                    "-XX:+UseShenandoahGC",
                                    "-verbose:gc",
                                    TestExplicitGC.class.getName(),
                                    "test");
            OutputAnalyzer output = new OutputAnalyzer(pb.start());
            for (String p : full) {
                output.shouldContain(p);
            }
            for (String p : concurrent) {
                output.shouldNotContain(p);
            }
        }

        {
            ProcessBuilder pb = ProcessTools.createJavaProcessBuilder(
                                    "-XX:+UseShenandoahGC",
                                    "-verbose:gc",
                                    "-XX:+DisableExplicitGC",
                                    TestExplicitGC.class.getName(),
                                    "test");
            OutputAnalyzer output = new OutputAnalyzer(pb.start());
            for (String p : full) {
                output.shouldNotContain(p);
            }
            for (String p : concurrent) {
                output.shouldNotContain(p);
            }
        }

        {
            ProcessBuilder pb = ProcessTools.createJavaProcessBuilder(
                                    "-XX:+UseShenandoahGC",
                                    "-verbose:gc",
                                    "-XX:+ExplicitGCInvokesConcurrent",
                                    TestExplicitGC.class.getName(),
                                    "test");
            OutputAnalyzer output = new OutputAnalyzer(pb.start());
            for (String p : full) {
                output.shouldNotContain(p);
            }
            for (String p : concurrent) {
                output.shouldContain(p);
            }
        }

        {
            ProcessBuilder pb = ProcessTools.createJavaProcessBuilder(
                                    "-XX:+UseShenandoahGC",
                                    "-verbose:gc",
                                    "-XX:-ExplicitGCInvokesConcurrent",
                                    TestExplicitGC.class.getName(),
                                    "test");
            OutputAnalyzer output = new OutputAnalyzer(pb.start());
            for (String p : full) {
                output.shouldContain(p);
            }
            for (String p : concurrent) {
                output.shouldNotContain(p);
            }
        }
    }
}
