package com.sci.jbfjit.jit;

public final class JITClassLoader extends ClassLoader
{
    public Class<?> loadClass(final String name, final byte[] data) throws ClassFormatError
    {
        return this.defineClass(name, data, 0, data.length);
    }
}