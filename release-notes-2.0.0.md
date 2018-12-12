#### Version Number
${version-number}

#### New Features
- [SCMOD-5144](https://autjira.microfocus.com/browse/SCMOD-5144): Workflow Id no longer required  
    Workflow Id can now be substituted for the name of the workflow to use.

#### Known Issues

#### Breaking Changes
- [SCMOD-5326](https://autjira.microfocus.com/browse/SCMOD-5326): Source parameter to workflow updated  
    Source parameter in workflow can now be either a string on an object under different circumstances, this is discussed and examples shown in documentation. TenantData Source is no longer supported and workflow produced now takes custom workflow configuration settings that are supplied to the script on the customdata for the document being processed.
