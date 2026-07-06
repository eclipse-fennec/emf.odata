/**
 * Eclipse Fennec OData – CSDL bundle.
 *
 * <p>Produces and consumes CSDL ({@code $metadata}) by mapping Fennec {@code EPackage}s
 * plus their OData metadata aspects to/from instances of the OASIS EDM/EDMX model
 * ({@code org.odata.csdl.model}), and letting EMF serialize/parse those instances. The
 * XML mapping is carried by the model itself (generated from the OASIS XSD via
 * ExtendedMetaData), so no hand-written CSDL reader/writer is required.
 */
@org.osgi.annotation.bundle.Export
@org.osgi.annotation.versioning.Version("1.0")
package org.eclipse.fennec.odata.csdl;
