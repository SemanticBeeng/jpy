package org.jpy.python;

/**
 * Native interface for the CPython interpreter.
 * <p/>
 * IMPORTANT NOTE: If you change the interface of this class, you will need to run {@code javah}, and then adapt {@code jni/org_jpy_python_PyLib.c}.
 *
 * @author Norman Fomferra
 */
public class PyLib {
    private static Throwable problem;

    static {
        try {
            if (PyConfig.getOS() == PyConfig.OS.UNIX) {
                // Even loading the Python shared lib does not solve our current problem on Unix:
                // java.lang.UnsatisfiedLinkError: /usr/local/lib/python3.3/dist-packages/jpy.cpython-33m.so:
                //      /usr/local/lib/python3.3/dist-packages/jpy.cpython-33m.so: undefined symbol: PyFloat_Type
                System.load("/usr/lib/libpython3.3m.so");
            }

            String sharedLibPath = PyConfig.getSharedLibPath();
            System.load(sharedLibPath);
            problem = null;
        } catch (Throwable t) {
            problem = t;
            throw t;
        }
    }

    public static void assertInterpreterInitialized() {
        if (problem != null) {
            throw new RuntimeException("PyLib not initialized", problem);
        }
        if (!isInterpreterInitialized()) {
            throw new RuntimeException("Python interpreter not initialized");
        }
    }

    public static native boolean isInterpreterInitialized();

    public static native boolean initializeInterpreter(String[] options, boolean debug);

    public static native void destroyInterpreter();

    public static native int execScript(String script);

    public static native void decref(long pointer);

    public static native int getIntValue(long pointer);

    public static native double getDoubleValue(long pointer);

    public static native String getStringValue(long pointer);

    public static native Object getObjectValue(long pointer);


    public static native long importModule(String name);

    /**
     * Gets the value of a given Python attribute as Python object pointer.
     *
     * @param pointer Identifies the Python object which contains the attribute {@code name}.
     * @param name    The attribute name.
     * @return Pointer to a Python object that is the value of the attribute (always a new reference).
     */
    public static native long getAttributeObject(long pointer, String name);

    /**
     * Gets the value of a given Python attribute as Java value.
     *
     * @param pointer    Identifies the Python object which contains the attribute {@code name}.
     * @param name       The attribute name.
     * @param valueType  The expected return type.
     * @return A value that represents the converted Python attribute value.
     */
    public static native <T> T getAttributeValue(long pointer, String name, Class<T> valueType);

    /**
     * Sets the Python attribute given by {@code name} of the Python object pointed to by {@code pointer}.
     * <p/>
     * Before the Python attribute is set, the Java {@code value} is converted into a corresponding
     * Python object using the optional {@code valueType}.
     * Todo: The {@code value} may also be of type {@code PyObject}.
     * This will be directly translated into the corresponding Python object without conversion.
     *
     * @param pointer   Identifies the Python object which contains the attribute {@code name}.
     * @param name      The attribute name.
     * @param value     The new attribute value.
     * @param valueType Optional type for converting the value to a Python object.
     */
    public static native <T> void setAttributeValue(long pointer, String name, T value, Class<T> valueType);

    /**
     * Calls a Python callable and returns the resulting Python object.
     * <p/>
     * Before the Python callable is called, the {@code args} array of Java objects is converted into corresponding
     * Python objects.
     * Todo: The {@code args} array may also contain objects of type {@code PyObject}.
     * These will be directly translated into the corresponding Python objects without conversion.
     *
     * @param pointer    Identifies the Python object which contains the callable {@code name}.
     * @param methodCall true, if this is a call of a method of the Python object pointed to by {@code pointer}.
     * @param name       The name of the callable.
     * @param argCount   The argument count (length of the following {@code args} array).
     * @param args       The arguments.
     * @param paramTypes Optional array of parameter types for the conversion of the {@code args} into a Python tuple.
     *                   If not null, it must be an array of the same length as {@code args}.
     * @return The resulting Python object (always a new reference).
     */
    public static native long callAndReturnObject(long pointer,
                                                  boolean methodCall,
                                                  String name,
                                                  int argCount,
                                                  Object[] args,
                                                  Class<?>[] paramTypes);

    /**
     * Calls a Python callable and returns the a Java Object.
     * <p/>
     * Before the Python callable is called, the {@code args} array of Java objects is converted into corresponding
     * Python objects. The return value of the Python call is converted to a Java object according the the given
     * return type.
     * Todo: The {@code args} array may also contain objects of type {@code PyObject}.
     * These will be directly translated into the corresponding Python objects without conversion.
     *
     * @param pointer    Identifies the Python object which contains the callable {@code name}.
     * @param methodCall true, if this is a call of a method of the Python object pointed to by {@code pointer}.
     * @param name       The name of the callable.
     * @param argCount   The argument count (length of the following {@code args} array).
     * @param args       The arguments.
     * @param paramTypes Optional array of parameter types for the conversion of the {@code args} into a Python tuple.
     *                   If not null, it must be an array of the same length as {@code args}.
     * @param returnType Optional return type.
     * @return The resulting Python object (always a new reference).
     */
    public static native <T> T callAndReturnValue(long pointer,
                                                  boolean methodCall,
                                                  String name,
                                                  int argCount,
                                                  Object[] args,
                                                  Class<?>[] paramTypes,
                                                  Class<T> returnType);

}
