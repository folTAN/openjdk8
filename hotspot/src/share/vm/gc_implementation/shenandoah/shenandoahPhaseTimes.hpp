/*
 * Copyright (c) 2016, Red Hat, Inc. and/or its affiliates.
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

#ifndef SHARE_VM_GC_SHENANDOAH_SHENANDOAHPHASETIMES_HPP
#define SHARE_VM_GC_SHENANDOAH_SHENANDOAHPHASETIMES_HPP

#include "memory/allocation.hpp"

template <class T> class ShenandoahWorkerDataArray;

class ShenandoahPhaseTimes : public CHeapObj<mtGC> {
 public:
  enum GCParPhases {
    ThreadRoots,
    CodeCacheRoots,
    StringTableRoots,
    UniverseRoots,
    JNIRoots,
    JNIWeakRoots,
    ObjectSynchronizerRoots,
    FlatProfilerRoots,
    ManagementRoots,
    SystemDictionaryRoots,
    CLDGRoots,
    JVMTIRoots,
    GCParPhasesSentinel
  };

 private:
  uint _max_gc_threads;
  ShenandoahWorkerDataArray<double>* _gc_par_phases[GCParPhasesSentinel];

 public:
  ShenandoahPhaseTimes(uint max_gc_threads);

  // record the time a phase took in seconds
  void record_time_secs(GCParPhases phase, uint worker_i, double secs);

  double average(uint i);
  void reset(uint i);
  void print();
};

class ShenandoahParPhaseTimesTracker : public StackObj {
  double _start_time;
  ShenandoahPhaseTimes::GCParPhases _phase;
  ShenandoahPhaseTimes* _phase_times;
  uint _worker_id;
public:
  ShenandoahParPhaseTimesTracker(ShenandoahPhaseTimes* phase_times, ShenandoahPhaseTimes::GCParPhases phase, uint worker_id);
  ~ShenandoahParPhaseTimesTracker();
};

#endif // SHARE_VM_GC_SHENANDOAH_SHENANDOAHPHASETIMES_HPP
