package de.haw.vs3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author moritzspindelhirn
 * @todo Documentation
 * @category de.haw.vs3
 */
public class Reader extends Thread {

    private String datasourceString = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    final Lock lock = new ReentrantLock();

    @Override
    public void run() {
        while(!isInterrupted()) {
            try {
                byte[] datasourceBytes = new byte[24];
                System.in.read(datasourceBytes);
                setDatasourceString(new String(datasourceBytes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getDatasourceString() {
        lock.lock();
        String retVasl = datasourceString;
        lock.unlock();

        return retVasl;
    }

    public void setDatasourceString(String datasourceString) {
        // not sure if lock is needed because strings are immutable and this is one operations, but better be safe.
        lock.lock();
        this.datasourceString = datasourceString;
        lock.unlock();
    }
}
