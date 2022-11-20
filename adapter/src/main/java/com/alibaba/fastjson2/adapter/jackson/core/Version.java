package com.alibaba.fastjson2.adapter.jackson.core;

public class Version
        implements Comparable<Version>, java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final Version UNKNOWN_VERSION = new Version(0, 0, 0, null, null, null);

    protected final int majorVersion;

    protected final int minorVersion;

    protected final int patchLevel;

    protected final String groupId;

    protected final String artifactId;

    /**
     * Additional information for snapshot versions; null for non-snapshot
     * (release) versions.
     */
    protected final String snapshotInfo;

    /**
     * @param major Major version number
     * @param minor Minor version number
     * @param patchLevel patch level of version
     * @param snapshotInfo Optional additional string qualifier
     * @since 2.1
     * @deprecated Use variant that takes group and artifact ids
     */
    @Deprecated
    public Version(int major, int minor, int patchLevel, String snapshotInfo) {
        this(major, minor, patchLevel, snapshotInfo, null, null);
    }

    public Version(int major, int minor, int patchLevel, String snapshotInfo,
                   String groupId, String artifactId) {
        majorVersion = major;
        minorVersion = minor;
        this.patchLevel = patchLevel;
        this.snapshotInfo = snapshotInfo;
        this.groupId = (groupId == null) ? "" : groupId;
        this.artifactId = (artifactId == null) ? "" : artifactId;
    }

    /**
     * Method returns canonical "not known" version, which is used as version
     * in cases where actual version information is not known (instead of null).
     *
     * @return Version instance to use as a placeholder when actual version is not known
     * (or not relevant)
     */
    public static Version unknownVersion() {
        return UNKNOWN_VERSION;
    }

    /**
     * @return {@code True} if this instance is the one returned by
     * call to {@link #unknownVersion()}
     * @since 2.7 to replace misspelled {@link #isUknownVersion()}
     */
    public boolean isUnknownVersion() {
        return (this == UNKNOWN_VERSION);
    }

    public boolean isSnapshot() {
        return (snapshotInfo != null && snapshotInfo.length() > 0);
    }

    /**
     * @return {@code True} if this instance is the one returned by
     * call to {@link #unknownVersion()}
     * @deprecated Since 2.7 use correctly spelled method {@link #isUnknownVersion()}
     */
    @Deprecated
    public boolean isUknownVersion() {
        return isUnknownVersion();
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getPatchLevel() {
        return patchLevel;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String toFullString() {
        return groupId + '/' + artifactId + '/' + toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(majorVersion).append('.');
        sb.append(minorVersion).append('.');
        sb.append(patchLevel);
        if (isSnapshot()) {
            sb.append('-').append(snapshotInfo);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return artifactId.hashCode() ^ groupId.hashCode() + majorVersion - minorVersion + patchLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        Version other = (Version) o;
        return (other.majorVersion == majorVersion)
                && (other.minorVersion == minorVersion)
                && (other.patchLevel == patchLevel)
                && other.artifactId.equals(artifactId)
                && other.groupId.equals(groupId);
    }

    @Override
    public int compareTo(Version other) {
        if (other == this) {
            return 0;
        }

        int diff = groupId.compareTo(other.groupId);
        if (diff == 0) {
            diff = artifactId.compareTo(other.artifactId);
            if (diff == 0) {
                diff = majorVersion - other.majorVersion;
                if (diff == 0) {
                    diff = minorVersion - other.minorVersion;
                    if (diff == 0) {
                        diff = patchLevel - other.patchLevel;
                    }
                }
            }
        }
        return diff;
    }
}
