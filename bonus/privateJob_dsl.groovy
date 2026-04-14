// --- Folder Settings ---
final String folderName = 'Bonus'
final String folderDescription = 'Folder for miscellaneous bonus.'

// --- Log Rotation Settings ---
final Integer maxBuildsToKeep = 10
final Integer maxArtifactsToKeep = 2

// --- Parameter Descriptions ---
final String githubNameDescription = 'GitHub repository owner/repo_name (e.g.: "EpitechIT31000/chocolatine")'
final String displayNameDescription = 'Display name for the job'

folder(folderName) {
    description(folderDescription)
}

final String privateJobTemplate = '''
    freeStyleJob(DISPLAY_NAME) {
        properties {
            githubProjectUrl("https://github.com/${GITHUB_NAME}")
        }
        scm {
            git {
                remote { 
                    url("https://github.com/${GITHUB_NAME}.git")
                    credentials('github-auth')
                }
                branches('master', 'main')
            }
        }
        triggers {
            scm('* * * * *')
        }
        wrappers {
            preBuildCleanup()
        }
        steps {
            shell(\'\'\'
                if [ -f Makefile ]; then 
                    make fclean
                    make
                    make tests_run
                    make clean
                    gcovr --exclude tests/ --xml-pretty -o coverage.xml
                elif [ -f CMakeLists.txt ]; then 
                    cmake -B build 
                    cmake --build build 
                    ctest --test-dir build
                    gcovr --exclude tests/ --xml-pretty -o coverage.xml
                else 
                    echo "No build files found" && exit 1
                fi
            \'\'\'.stripIndent())
        }
        publishers {
            recordCoverage {
                tools {
                    coverageTool {
                        parser('COBERTURA')
                        pattern('coverage.xml')
                    }
                }
            }
        }
    }
'''.stripIndent()

freeStyleJob("${folderName}/clone-private-repository") {
    description('Job to clone a private Git repository using a provided URL and credentials.')

    parameters {
        stringParam('GIT_REPOSITORY_URL', '', 'HTTPS URL of the private repository (https://github.com/user/repo.git)')
    }

    wrappers {
        preBuildCleanup()
    }

    scm {
        git {
            remote {
                url('$GIT_REPOSITORY_URL')
                credentials('github-auth')
            }
            branches('*/master', '*/main')
        }
    }

    logRotator {
        numToKeep(maxBuildsToKeep)
        artifactNumToKeep(maxArtifactsToKeep)
    }
}

freeStyleJob("${folderName}/SEED-PRIVATE") {
    description('Seed job to create new Jenkins jobs based on a private GitHub repository using credentials.')

    parameters {
        stringParam('GITHUB_NAME', '', githubNameDescription)
        stringParam('DISPLAY_NAME', '', displayNameDescription)
    }

    steps {
        dsl {
            text(privateJobTemplate)
        }
    }

    logRotator {
        numToKeep(maxBuildsToKeep)
        artifactNumToKeep(maxArtifactsToKeep)
    }
}
