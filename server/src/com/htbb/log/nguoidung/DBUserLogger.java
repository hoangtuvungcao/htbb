/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop va hanh vi; package da doi sang com.htbb.log.nguoidung (bo ten game cu). Nguon goc:
 * docs/giay-phep.md.
 */
package com.htbb.log.nguoidung;

import java.util.concurrent.LinkedBlockingQueue;

public class DBUserLogger implements Runnable {
    public static DBUserLogger instance = new DBUserLogger();
    public LinkedBlockingQueue<String> userLogQueue = new LinkedBlockingQueue<>();

    @Override
    public void run() {
        Thread.currentThread().setName("DBUserLogger");

        while (true) {
            try {
                if (this.userLogQueue.size() > 100) {
                    this.userLogQueue.clear();
                }

                String url = this.userLogQueue.take();
                Net.getHttpAsync(url);
            } catch (Exception var2) {
                this.userLogQueue = new LinkedBlockingQueue<>();
            }
        }
    }
}
