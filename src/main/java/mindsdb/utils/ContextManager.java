package mindsdb.utils;

// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.ConcurrentMap;

// public class ContextManager {
//     private static final ThreadLocal<ConcurrentMap<String, String>> contextStorage = ThreadLocal
//             .withInitial(() -> {
//                 System.err.println("Initializing contextStorage for thread: " + Thread.currentThread().getName());
//                 return new ConcurrentHashMap<>();
//             });

//     public static void setContext(String name, String value) {
//         System.err.println("contextStorage is null for thread: " + Thread.currentThread().getName());
//         contextStorage.get().put(name, value);
//     }

//     public static String getContext(String name) {
//         return contextStorage.get().get(name);
//     }

//     public static void setSaving(String name) {
//         setContext("saving", name);
//     }

//     public static boolean isSaving() {
//         return getContext("saving") != null;
//     }
// }

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ContextManager {
    private static final AtomicReference<Map<String, String>> contextStorage = new AtomicReference<>(new HashMap<>());

    public static void setContext(String name, String value) {
        Map<String, String> map = contextStorage.get();
        if (map == null) {
            System.err.println("contextStorage is null");
        } else {
            map.put(name, value);
        }
    }

    public static String getContext(String name) {
        Map<String, String> map = contextStorage.get();
        if (map == null) {
            System.err.println("contextStorage is null");
            return null;
        } else {
            return map.get(name);
        }
    }

    public static void setSaving(String name) {
        setContext("saving", name);
    }

    public static boolean isSaving() {
        return getContext("saving") != null;
    }
}