package com.csa09.events;

import com.csa09.events.dao.InMemoryRegistrationDao;
import com.csa09.events.model.Registration;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/** Task 6 alternative analysis: load ALL rows into a table model vs ONE page of 20 rows. */
public class LoadBenchmark {
    static long used() { Runtime r = Runtime.getRuntime(); System.gc(); System.gc(); return (r.totalMemory() - r.freeMemory()) / 1024; }

    public static void main(String[] args) throws Exception {
        for (int n : new int[]{1_000, 10_000, 100_000}) {
            InMemoryRegistrationDao dao = new InMemoryRegistrationDao(n);
            long base = used();
            long t0 = System.nanoTime();
            List<Registration> all = dao.all(null);                               // approach A: everything at once
            DefaultTableModel big = new DefaultTableModel(new String[]{"ID", "Reg", "Name", "Email", "Event", "At"}, 0);
            for (Registration r : all) big.addRow(new Object[]{r.id(), r.studentReg(), r.studentName(), r.email(), r.eventName(), r.registeredAt().toString()});
            double msAll = (System.nanoTime() - t0) / 1e6;
            long kbAll = used() - base;
            if (big.getRowCount() != all.size()) throw new IllegalStateException();   // keeps objects reachable until measured
            all = null; big = null;
            base = used();
            long t1 = System.nanoTime();
            List<Registration> pg = dao.page(null, 40, 20);                       // approach B: one page
            DefaultTableModel small = new DefaultTableModel(new String[]{"ID", "Reg", "Name", "Email", "Event", "At"}, 0);
            for (Registration r : pg) small.addRow(new Object[]{r.id(), r.studentReg(), r.studentName(), r.email(), r.eventName(), r.registeredAt().toString()});
            double msPage = (System.nanoTime() - t1) / 1e6;
            System.out.printf("n=%-7d  load-all: %8.1f ms, ~%6d KB of table data   |   one page (20 rows): %6.2f ms, ~%d KB%n",
                n, msAll, kbAll, msPage, Math.max(1, used() - base));
        }
    }
}
