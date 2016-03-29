package de.s2.gsim.def;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import de.s2.gsim.def.objects.FrameOLD;
import de.s2.gsim.util.Logging;

public class FramePersistenceManager {

    private String namespace;

    private String persistDir = "../persistence/frames";

    public FramePersistenceManager(String namespace) {
        this.namespace = namespace;
    }

    public void deleteFrame(FrameOLD f) {
        deletePermanently(f);
    }

    public FrameOLD[] getAllFrames() {

        long a = 0, b = 0;
        a = System.currentTimeMillis();
        ArrayList list = new ArrayList();
        try {
            File dir = new File(persistDir + "/" + namespace);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File[] frames = dir.listFiles(new Filter());

            for (int i = 0; i < frames.length; i++) {
                FileInputStream fi = new FileInputStream(frames[i].getCanonicalPath());
                ObjectInputStream si = new ObjectInputStream(fi);
                FrameOLD inst = (FrameOLD) si.readObject();
                si.close();
                list.add(inst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        b = System.currentTimeMillis() - a;
        Logging.ModelLogger.info("Loaded " + list.size() + " frames in " + (double) b / 1000 + " seconds.");
        FrameOLD[] res = new FrameOLD[list.size()];
        list.toArray(res);
        return res;
    }

    public FrameOLD reload(String name) {
        try {

            name = name.replace('\\', '_');
            name = name.replace('/', '_');

            File file = new File(persistDir + "/" + namespace + "/" + name).getCanonicalFile();
            FileInputStream fi = new FileInputStream(file.getCanonicalPath());
            ObjectInputStream si = new ObjectInputStream(fi);
            FrameOLD inst = (FrameOLD) si.readObject();
            si.close();
            return inst;
        } catch (FileNotFoundException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveFrame(FrameOLD f) {
        savePermanently(f);
    }

    public void saveFrames(FrameOLD[] pf) {
        for (int i = 0; i < pf.length; i++) {
            saveFrame(pf[i]);
        }
    }

    private void deletePermanently(FrameOLD c) {
        File dir = new File(persistDir + "/" + namespace);

        if (dir.exists()) {
            File[] content = dir.listFiles();
            String name = c.getTypeName();
            name = name.replace('\\', '_');
            name = name.replace('/', '_');
            for (int i = 0; i < content.length; i++) {
                if (content[i].getName().equals(name)) {
                    content[i].delete();
                    return;
                }
            }
        } else {
            dir.mkdir();
        }
    }

    private void savePermanently(FrameOLD inst) {
        try {

            File dir = new File(persistDir + "/" + namespace);
            if (!dir.exists()) {
                dir.mkdirs();
                File f = new File("../persistence/" + namespace);
                f.createNewFile();
            }
            new File("../persistence/" + namespace).createNewFile();

            String name = inst.getTypeName();
            name = name.replace('\\', '_');
            name = name.replace('/', '_');

            FileOutputStream fo = new FileOutputStream(dir.getCanonicalPath() + "/" + name);
            ObjectOutputStream so = new ObjectOutputStream(fo);
            so.writeObject(inst);
            so.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class Filter implements FilenameFilter {
        public Filter() {
        }

        // accept all those that do not begin with "."
        @Override
        public boolean accept(File f, String s) {
            if (s.indexOf(".") < 0) {
                return true;
            }
            if (s.indexOf(".") > 0) {
                return true;
            }

            return false;
        }

    }
}
