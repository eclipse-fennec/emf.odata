/**
 * Eclipse Fennec OData – Metadata bundle (E1 Foundation).
 *
 * <p>Hosts the {@code ODataMetadataHandler}, which docks onto the Model Metadata Service via its
 * {@link org.eclipse.fennec.emf.osgi.metadata.MetadataHandler} SPI: registered with the
 * {@link org.eclipse.fennec.emf.osgi.metadata.MetadataWhiteboard}, it resolves the per-EPackage
 * OData profile on {@code registerPackage()} and attaches it as the {@code "odata"} aspect entry.
 *
 * <p>The bundle carries no Ecore of its own — the aspect content is the standalone profile model
 * owned by the CSDL converter (ADR-0003).
 */
package org.eclipse.fennec.odata.metadata;
