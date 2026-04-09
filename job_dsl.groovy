
// --- Folder Settings ---
final String folderName = 'Tools'
final String folderDescription = 'Folder for miscellaneous tools.'

// --- Log Rotation Settings ---
final Integer maxBuildsToKeep = 10
final Integer maxArtifactsToKeep = 2

// --- Parameter Descriptions ---
final String githubNameDescription = 'GitHub repository owner/repo_name (e.g.: "EpitechIT31000/chocolatine")'
final String displayNameDescription = 'Display name for the job'

// --- Job Template ---
/* groovylint-disable-next-line GStringExpressionWithinString */
final String jobTemplate = '''
    freeStyleJob(DISPLAY_NAME) {
        properties {
            githubProjectUrl("https://github.com/${GITHUB_NAME}")
        }
        scm {
            git {
                remote { url("https://github.com/${GITHUB_NAME}.git") }
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
            shell('make fclean')
            shell('make')
            shell('make tests_run')
            shell('make clean')
        }
    }
'''.stripIndent()

folder(folderName) {
    description(folderDescription)
}

freeStyleJob("${folderName}/clone-repository") {
    description('Job to clone a Git repository using a provided URL.')

    parameters {
        stringParam('GIT_REPOSITORY_URL', '', 'Git URL of the repository to clone')
    }

    wrappers {
        preBuildCleanup()
    }

    steps {
        shell('git clone $GIT_REPOSITORY_URL')
    }

    logRotator {
        numToKeep(maxBuildsToKeep)
        artifactNumToKeep(maxArtifactsToKeep)
    }
}

freeStyleJob("${folderName}/SEED") {
    description('Seed job to create new Jenkins jobs based on a GitHub repository.')

    parameters {
        stringParam('GITHUB_NAME', '', githubNameDescription)
        stringParam('DISPLAY_NAME', '', displayNameDescription)
    }

    steps {
        dsl {
            text(jobTemplate)
        }
    }

    logRotator {
        numToKeep(maxBuildsToKeep)
        artifactNumToKeep(maxArtifactsToKeep)
    }
}
