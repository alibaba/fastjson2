// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package com.alibaba.fastjson2.internal.asm;

/**
 * The constant pool entries, the BootstrapMethods attribute entries and the (ASM specific) type
 * table entries of a class.
 *
 * @author Eric Bruneton
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.4">JVMS
 * 4.4</a>
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.23">JVMS
 * 4.7.23</a>
 */
final class SymbolTable {
    final ClassWriter classWriter;

    /**
     * The major version number of the class to which this symbol table belongs.
     */
    private int majorVersion;

    /**
     * The internal name of the class to which this symbol table belongs.
     */
    String className;

    /**
     * The total number of {@link Entry} instances in {@link #entries}. This includes entries that are
     * accessible (recursively) via {@link Entry#next}.
     */
    private int entryCount;

    /**
     * A hash set of all the entries in this SymbolTable (this includes the constant pool entries, the
     * bootstrap method entries and the type table entries). Each {@link Entry} instance is stored at
     * the array index given by its hash code modulo the array size. If several entries must be stored
     * at the same array index, they are linked together via their {@link Entry#next} field. The
     * factory methods of this class make sure that this table does not contain duplicated entries.
     */
    private Entry[] entries;

    /**
     * The number of constant pool items in {@link #constantPool}, plus 1. The first constant pool
     * item has index 1, and long and double items count for two items.
     */
    int constantPoolCount;

    /**
     * The content of the ClassFile's constant_pool JVMS structure corresponding to this SymbolTable.
     * The ClassFile's constant_pool_count field is <i>not</i> included.
     */
    ByteVector constantPool;

    /**
     * The actual number of elements in {@link #typeTable}. These elements are stored from index 0 to
     * typeCount (excluded). The other array entries are empty.
     */
    private int typeCount;
    Entry[] typeTable;

    /**
     * Constructs a new, empty SymbolTable for the given ClassWriter.
     *
     * @param classWriter a ClassWriter.
     */
    SymbolTable(final ClassWriter classWriter) {
        this.classWriter = classWriter;
        this.entries = new Entry[256];
        this.constantPoolCount = 1;
        this.constantPool = new ByteVector(4096);
    }
//
//    /**
//     * Returns the internal name of the class to which this symbol table belongs.
//     *
//     * @return the internal name of the class to which this symbol table belongs.
//     */
//    String getClassName() {
//        return className;
//    }

    /**
     * Sets the major version and the name of the class to which this symbol table belongs. Also adds
     * the class name to the constant pool.
     *
     * @param majorVersion a major ClassFile version number.
     * @param className    an internal class name.
     * @return the constant pool index of a new or already existing Symbol with the given class name.
     */
    int setMajorVersionAndClassName(final int majorVersion, final String className) {
        this.majorVersion = majorVersion;
        this.className = className;
        return addConstantUtf8Reference(/*CONSTANT_CLASS_TAG*/ 7, className).index;
    }
//
//    /**
//     * Returns the length in bytes of this symbol table's constant_pool array.
//     *
//     * @return the length in bytes of this symbol table's constant_pool array.
//     */
//    int getConstantPoolLength() {
//        return constantPool.length;
//    }

//    /**
//     * Puts this symbol table's constant_pool array in the given ByteVector, preceded by the
//     * constant_pool_count value.
//     *
//     * @param output where the JVMS ClassFile's constant_pool array must be put.
//     */
//    void putConstantPool(final ByteVector output) {
//        output.putShort(constantPoolCount).putByteArray(constantPool.data, 0, constantPool.length);
//    }
//
//    /**
//     * Returns the size in bytes of this symbol table's BootstrapMethods attribute. Also adds the
//     * attribute name in the constant pool.
//     *
//     * @return the size in bytes of this symbol table's BootstrapMethods attribute.
//     */
//    int computeBootstrapMethodsSize() {
//        return 0;
//    }
//
//    /**
//     * Puts this symbol table's BootstrapMethods attribute in the given ByteVector. This includes the
//     * 6 attribute header bytes and the num_bootstrap_methods value.
//     *
//     * @param output where the JVMS BootstrapMethods attribute must be put.
//     */
//    void putBootstrapMethods(final ByteVector output) {
//    }

    // -----------------------------------------------------------------------------------------------
    // Generic symbol table entries management.
    // -----------------------------------------------------------------------------------------------
//
//    /**
//     * Returns the list of entries which can potentially have the given hash code.
//     *
//     * @param hashCode a {@link Entry#hashCode} value.
//     * @return the list of entries which can potentially have the given hash code. The list is stored
//     * via the {@link Entry#next} field.
//     */
//    private Entry get(final int hashCode) {
//        return entries[hashCode % entries.length];
//    }

    /**
     * Puts the given entry in the {@link #entries} hash set. This method does <i>not</i> check
     * whether {@link #entries} already contains a similar entry or not. {@link #entries} is resized
     * if necessary to avoid hash collisions (multiple entries needing to be stored at the same {@link
     * #entries} array index) as much as possible, with reasonable memory usage.
     *
     * @param entry an Entry (which must not already be contained in {@link #entries}).
     * @return the given entry
     */
    private Entry put(final Entry entry) {
        if (entryCount > (entries.length * 3) / 4) {
            int currentCapacity = entries.length;
            int newCapacity = currentCapacity * 2 + 1;
            Entry[] newEntries = new Entry[newCapacity];
            for (int i = currentCapacity - 1; i >= 0; --i) {
                Entry currentEntry = entries[i];
                while (currentEntry != null) {
                    int newCurrentEntryIndex = currentEntry.hashCode % newCapacity;
                    Entry nextEntry = currentEntry.next;
                    currentEntry.next = newEntries[newCurrentEntryIndex];
                    newEntries[newCurrentEntryIndex] = currentEntry;
                    currentEntry = nextEntry;
                }
            }
            entries = newEntries;
        }
        entryCount++;
        int index = entry.hashCode % entries.length;
        entry.next = entries[index];
        return entries[index] = entry;
    }

    // -----------------------------------------------------------------------------------------------
    // Constant pool entries management.
    // -----------------------------------------------------------------------------------------------

//  /**
//   * Adds a number or string constant to the constant pool of this symbol table. Does nothing if the
//   * constant pool already contains a similar item.
//   *
//   * @param value the value of the constant to be added to the constant pool. This parameter must be
//   *     an {@link Integer}, {@link Byte}, {@link Character}, {@link Short}, {@link Boolean}, {@link
//   *     Float}, {@link Long}, {@link Double}, {@link String}, {@link Type}.
//   * @return a new or already existing Symbol with the given value.
//   */
//  Symbol addConstant(final Object value) {
//    if (value instanceof Integer) {
//      return addConstantIntegerOrFloat(Symbol.CONSTANT_INTEGER_TAG, ((Integer) value).intValue());
//    } else if (value instanceof Byte) {
//      return addConstantIntegerOrFloat(Symbol.CONSTANT_INTEGER_TAG, ((Byte) value).intValue());
//    } else if (value instanceof Character) {
//      return addConstantIntegerOrFloat(Symbol.CONSTANT_INTEGER_TAG, ((Character) value).charValue());
//    } else if (value instanceof Short) {
//      return addConstantIntegerOrFloat(Symbol.CONSTANT_INTEGER_TAG, ((Short) value).intValue());
//    } else if (value instanceof Boolean) {
//      return addConstantIntegerOrFloat(Symbol.CONSTANT_INTEGER_TAG, ((Boolean) value).booleanValue() ? 1 : 0);
//    } else if (value instanceof Float) {
//      float floatValue = ((Float) value).floatValue();
//      return addConstantIntegerOrFloat(Symbol.CONSTANT_FLOAT_TAG, Float.floatToRawIntBits(floatValue));
//    } else if (value instanceof Long) {
//      return addConstantLongOrDouble(Symbol.CONSTANT_LONG_TAG, ((Long) value).longValue());
//    } else if (value instanceof Double) {
//      double doubleValue = ((Double) value).doubleValue();
//      return addConstantLongOrDouble(Symbol.CONSTANT_DOUBLE_TAG, Double.doubleToRawLongBits(doubleValue));
//    } else if (value instanceof String) {
//      return addConstantUtf8Reference(Symbol.CONSTANT_STRING_TAG, (String) value);
//    } else if (value instanceof Type) {
//      Type type = (Type) value;
//      int typeSort = type.getSort();
//      if (typeSort == Type.OBJECT) {
//        return addConstantClass(type.getInternalName());
//      } else if (typeSort == Type.METHOD) {
//        return addConstantUtf8Reference(Symbol.CONSTANT_METHOD_TYPE_TAG, type.getDescriptor());
//      } else { // type is a primitive or array type.
//        return addConstantClass(type.getDescriptor());
//      }
//    } else {
//      throw new IllegalArgumentException("value " + value);
//    }
//  }
//
//    /**
//     * Adds a CONSTANT_Class_info to the constant pool of this symbol table. Does nothing if the
//     * constant pool already contains a similar item.
//     *
//     * @param value the internal name of a class.
//     * @return a new or already existing Symbol with the given value.
//     */
//    Symbol addConstantClass(final String value) {
//        final int CONSTANT_CLASS_TAG = 7;
//        return addConstantUtf8Reference(CONSTANT_CLASS_TAG, value);
//    }
//
//    /**
//     * Adds a CONSTANT_Fieldref_info to the constant pool of this symbol table. Does nothing if the
//     * constant pool already contains a similar item.
//     *
//     * @param owner      the internal name of a class.
//     * @param name       a field name.
//     * @param descriptor a field descriptor.
//     * @return a new or already existing Symbol with the given value.
//     */
//    Symbol addConstantFieldref(final String owner, final String name, final String descriptor) {
//        return addConstantMemberReference(CONSTANT_FIELDREF_TAG, owner, name, descriptor);
//    }
//
//    /**
//     * Adds a CONSTANT_Methodref_info or CONSTANT_InterfaceMethodref_info to the constant pool of this
//     * symbol table. Does nothing if the constant pool already contains a similar item.
//     *
//     * @param owner       the internal name of a class.
//     * @param name        a method name.
//     * @param descriptor  a method descriptor.
//     * @param isInterface whether owner is an interface or not.
//     * @return a new or already existing Symbol with the given value.
//     */
//    Symbol addConstantMethodref(
//            final String owner, final String name, final String descriptor, final boolean isInterface) {
//        final int CONSTANT_METHODREF_TAG = 10;
//        final int CONSTANT_INTERFACE_METHODREF_TAG = 11;
//        int tag = isInterface ? /*CONSTANT_INTERFACE_METHODREF_TAG*/ 11 : /*CONSTANT_METHODREF_TAG*/ 10;
//        return addConstantMemberReference(
//                isInterface ? /*CONSTANT_INTERFACE_METHODREF_TAG*/ 11 : /*CONSTANT_METHODREF_TAG*/ 10
//                , owner, name, descriptor
//        );
//    }

    Entry addConstantMemberReference(
            final int tag, final String owner, final String name, final String descriptor) {
//    int hashCode = hash(tag, owner, name, descriptor);
        int hashCode = 0x7FFFFFFF & (tag + owner.hashCode() * name.hashCode() * descriptor.hashCode());
        Entry entry = entries[hashCode % entries.length];
        while (entry != null) {
            if (entry.tag == tag
                    && entry.hashCode == hashCode
                    && entry.owner.equals(owner)
                    && entry.name.equals(name)
                    && entry.value.equals(descriptor)) {
                return entry;
            }
            entry = entry.next;
        }
        constantPool.put122(
                tag, addConstantUtf8Reference(/*CONSTANT_CLASS_TAG*/ 7, owner).index, addConstantNameAndType(name, descriptor));
        return put(new Entry(constantPoolCount++, tag, owner, name, descriptor, 0, hashCode));
    }

    Symbol addConstantIntegerOrFloat(final int tag, final int value) {
        int hashCode = 0x7FFFFFFF & (tag + value);
        Entry entry = entries[hashCode % entries.length];
        while (entry != null) {
            if (entry.tag == tag && entry.hashCode == hashCode && entry.data == value) {
                return entry;
            }
            entry = entry.next;
        }
        constantPool.putByte(tag).putInt(value);
        return put(new Entry(constantPoolCount++, tag, value, hashCode));
    }

    Symbol addConstantLongOrDouble(final int tag, final long value) {
        int hashCode = 0x7FFFFFFF & (tag + (int) value + (int) (value >>> 32));
        Entry entry = entries[hashCode % entries.length];
        while (entry != null) {
            if (entry.tag == tag && entry.hashCode == hashCode && entry.data == value) {
                return entry;
            }
            entry = entry.next;
        }
        int index = constantPoolCount;
        constantPool.putByte(tag).putLong(value);
        constantPoolCount += 2;
        return put(new Entry(index, tag, value, hashCode));
    }

    /**
     * Adds a CONSTANT_NameAndType_info to the constant pool of this symbol table. Does nothing if the
     * constant pool already contains a similar item.
     *
     * @param name       a field or method name.
     * @param descriptor a field or method descriptor.
     * @return a new or already existing Symbol with the given value.
     */
    int addConstantNameAndType(final String name, final String descriptor) {
        final int CONSTANT_NAME_AND_TYPE_TAG = 12;
        final int tag = CONSTANT_NAME_AND_TYPE_TAG;
        int hashCode = 0x7FFFFFFF & (tag + name.hashCode() * descriptor.hashCode());
        Entry entry = entries[hashCode % entries.length];
        while (entry != null) {
            if (entry.tag == tag
                    && entry.hashCode == hashCode
                    && entry.name.equals(name)
                    && entry.value.equals(descriptor)) {
                return entry.index;
            }
            entry = entry.next;
        }
        constantPool.put122(tag, addConstantUtf8(name), addConstantUtf8(descriptor));
        return put(new Entry(constantPoolCount++, tag, name, descriptor, hashCode)).index;
    }

    /**
     * Adds a CONSTANT_Utf8_info to the constant pool of this symbol table. Does nothing if the
     * constant pool already contains a similar item.
     *
     * @param value a string.
     * @return a new or already existing Symbol with the given value.
     */
    int addConstantUtf8(final String value) {
        final int CONSTANT_UTF8_TAG = 1;

        int hashCode = 0x7FFFFFFF & (CONSTANT_UTF8_TAG + value.hashCode());
        Entry entry = entries[hashCode % entries.length];
        while (entry != null) {
            if (entry.tag == CONSTANT_UTF8_TAG
                    && entry.hashCode == hashCode
                    && entry.value.equals(value)) {
                return entry.index;
            }
            entry = entry.next;
        }
        constantPool.putByte(CONSTANT_UTF8_TAG).putUTF8(value);
        return put(new Entry(constantPoolCount++, CONSTANT_UTF8_TAG, value, hashCode)).index;
    }

    Symbol addConstantUtf8Reference(final int tag, final String value) {
        int hashCode = 0x7FFFFFFF & (tag + value.hashCode());
        Entry entry = entries[hashCode % entries.length];
        while (entry != null) {
            if (entry.tag == tag && entry.hashCode == hashCode && entry.value.equals(value)) {
                return entry;
            }
            entry = entry.next;
        }
        constantPool.put12(tag, addConstantUtf8(value));
        return put(new Entry(constantPoolCount++, tag, value, hashCode));
    }
//
//    /**
//     * Returns the type table element whose index is given.
//     *
//     * @param typeIndex a type table index.
//     * @return the type table element whose index is given.
//     */
//    Symbol getType(final int typeIndex) {
//        return typeTable[typeIndex];
//    }

    /**
     * Adds a type in the type table of this symbol table. Does nothing if the type table already
     * contains a similar type.
     *
     * @param value an internal class name.
     * @return the index of a new or already existing type Symbol with the given value.
     */
    int addType(final String value) {
        final int TYPE_TAG = 128;

        int hashCode = 0x7FFFFFFF & (TYPE_TAG + value.hashCode());
        Entry entry = entries[hashCode % entries.length];
        while (entry != null) {
            if (entry.tag == TYPE_TAG && entry.hashCode == hashCode && entry.value.equals(value)) {
                return entry.index;
            }
            entry = entry.next;
        }
        return addTypeInternal(new Entry(typeCount, TYPE_TAG, value, hashCode));
    }

    /**
     * Adds an {@link Frame#ITEM_UNINITIALIZED} type in the type table of this symbol table. Does
     * nothing if the type table already contains a similar type.
     *
     * @param value          an internal class name.
     * @param bytecodeOffset the bytecode offset of the NEW instruction that created this {@link
     *                       Frame#ITEM_UNINITIALIZED} type value.
     * @return the index of a new or already existing type Symbol with the given value.
     */
    int addUninitializedType(final String value, final int bytecodeOffset) {
        final int UNINITIALIZED_TYPE_TAG = 129;

        int hashCode = 0x7FFFFFFF & (UNINITIALIZED_TYPE_TAG + value.hashCode() + bytecodeOffset);
        Entry entry = entries[hashCode % entries.length];
        while (entry != null) {
            if (entry.tag == UNINITIALIZED_TYPE_TAG
                    && entry.hashCode == hashCode
                    && entry.data == bytecodeOffset
                    && entry.value.equals(value)) {
                return entry.index;
            }
            entry = entry.next;
        }
        return addTypeInternal(
                new Entry(typeCount, UNINITIALIZED_TYPE_TAG, value, bytecodeOffset, hashCode));
    }

    int addMergedType(final int typeTableIndex1, final int typeTableIndex2) {
        final int MERGED_TYPE_TAG = 130;

        long data =
                typeTableIndex1 < typeTableIndex2
                        ? typeTableIndex1 | (((long) typeTableIndex2) << 32)
                        : typeTableIndex2 | (((long) typeTableIndex1) << 32);
        int hashCode = 0x7FFFFFFF & (MERGED_TYPE_TAG + typeTableIndex1 + typeTableIndex2);
        Entry entry = entries[hashCode % entries.length];
        while (entry != null) {
            if (entry.tag == MERGED_TYPE_TAG && entry.hashCode == hashCode && entry.data == data) {
                return entry.info;
            }
            entry = entry.next;
        }
        String type1 = typeTable[typeTableIndex1].value;
        String type2 = typeTable[typeTableIndex2].value;
        int commonSuperTypeIndex = addType(classWriter.getCommonSuperClass(type1, type2));
        put(new Entry(typeCount, MERGED_TYPE_TAG, data, hashCode)).info = commonSuperTypeIndex;
        return commonSuperTypeIndex;
    }

    private int addTypeInternal(final Entry entry) {
        if (typeTable == null) {
            typeTable = new Entry[16];
        }
        if (typeCount == typeTable.length) {
            Entry[] newTypeTable = new Entry[2 * typeTable.length];
            System.arraycopy(typeTable, 0, newTypeTable, 0, typeTable.length);
            typeTable = newTypeTable;
        }
        typeTable[typeCount++] = entry;
        return put(entry).index;
    }

    /**
     * An entry of a SymbolTable. This concrete and private subclass of {@link Symbol} adds two fields
     * which are only used inside SymbolTable, to implement hash sets of symbols (in order to avoid
     * duplicate symbols). See {@link #entries}.
     *
     * @author Eric Bruneton
     */
    static class Entry
            extends Symbol {
        /**
         * The hash code of this entry.
         */
        final int hashCode;

        /**
         * Another entry (and so on recursively) having the same hash code (modulo the size of {@link
         * #entries}) as this one.
         */
        Entry next;

        Entry(
                final int index,
                final int tag,
                final String owner,
                final String name,
                final String value,
                final long data,
                final int hashCode) {
            super(index, tag, owner, name, value, data);
            this.hashCode = hashCode;
        }

        Entry(final int index, final int tag, final String value, final int hashCode) {
            super(index, tag, /* owner = */ null, /* name = */ null, value, /* data = */ 0);
            this.hashCode = hashCode;
        }

        Entry(final int index, final int tag, final String value, final long data, final int hashCode) {
            super(index, tag, /* owner = */ null, /* name = */ null, value, data);
            this.hashCode = hashCode;
        }

        Entry(
                final int index, final int tag, final String name, final String value, final int hashCode) {
            super(index, tag, /* owner = */ null, name, value, /* data = */ 0);
            this.hashCode = hashCode;
        }

        Entry(final int index, final int tag, final long data, final int hashCode) {
            super(index, tag, /* owner = */ null, /* name = */ null, /* value = */ null, data);
            this.hashCode = hashCode;
        }
    }
}
