package gsim.def;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import gsim.def.objects.Instance;
import gsim.util.Logging;

public class InstancePersistenceManager {

    private String namespace;

    private String persistDir = "../persistence/instances";

    public InstancePersistenceManager(String namespace) {
        this.namespace = namespace;
        loadAll();
    }

    public void deleteInstance(Instance c) {
        deletePersistently(c);
    }

    public Instance[] loadAll() {

        long a = 0, b = 0;
        a = System.currentTimeMillis();
        ArrayList list = new ArrayList();
        try {
            File dir = new File(persistDir + "/" + namespace);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File[] instances = dir.listFiles(new Filter());
            for (int i = 0; i < instances.length; i++) {
                FileInputStream fi = new FileInputStream(instances[i].getCanonicalPath());
                ObjectInputStream si = new ObjectInputStream(fi);
                Instance inst = (Instance) si.readObject();
                si.close();

                list.add(inst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        b = System.currentTimeMillis() - a;
        Logging.ModelLogger.info("Loaded " + list.size() + " instances in " + (double) b / 1000 + " seconds.");

        Instance[] res = new Instance[list.size()];
        list.toArray(res);
        return res;
    }

    public Instance reload(String name) {
        try {
            name = name.replace('\\', '_');
            name = name.replace('/', '_');

            File file = new File(persistDir + "/" + namespace + "/" + name).getCanonicalFile();
            FileInputStream fi = new FileInputStream(file.getCanonicalPath());
            ObjectInputStream si = new ObjectInputStream(fi);
            Instance inst = (Instance) si.readObject();
            si.close();
            return inst;
        } catch (FileNotFoundException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveInstance(Instance c) {
        savePersistently(c);
    }

    private void deletePersistently(Instance c) {
        File dir = new File(persistDir + "/" + namespace);

        if (dir.exists()) {
            File[] content = dir.listFiles();
            String name = c.getName();
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

    private void savePersistently(Instance inst) {
        try {
            File dir = new File(persistDir + "/" + namespace);
            if (!dir.exists()) {
                dir.mkdirs();
                File f = new File("../persistence/" + namespace);
                f.createNewFile();
            }
            new File("../persistence/" + namespace).createNewFile();

            String name = inst.getName();
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

        @Override
        public boolean accept(File f, String s) {
            if (s.indexOf(".log") == -1) {
                return true;
            }
            return false;
        }
    }
}
